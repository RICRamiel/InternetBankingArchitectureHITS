package org.ricramiel.monitoringservice.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ricramiel.common.headers.CustomHeaders;
import org.ricramiel.monitoringservice.dto.MetricResponse;
import org.ricramiel.monitoringservice.dto.ServiceSummaryResponse;
import org.ricramiel.monitoringservice.dto.SummaryResponse;
import org.ricramiel.monitoringservice.dto.TimeBucketResponse;
import org.ricramiel.monitoringservice.entity.MetricEntity;
import org.ricramiel.monitoringservice.repository.MetricRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsController {
    private final MetricRepository metricRepository;

    @GetMapping("/summary")
    public SummaryResponse summary(@RequestParam(name = "minutes", defaultValue = "60") int minutes) {
        return summarize(recent(minutes));
    }

    @GetMapping("/services")
    public List<ServiceSummaryResponse> services(@RequestParam(name = "minutes", defaultValue = "60") int minutes) {
        return recent(minutes).stream()
                .collect(Collectors.groupingBy(metric -> safe(metric.getServiceName())))
                .entrySet()
                .stream()
                .map(entry -> {
                    SummaryResponse summary = summarize(entry.getValue());
                    return new ServiceSummaryResponse(
                            entry.getKey(),
                            summary.total(),
                            summary.errors(),
                            summary.errorRate(),
                            summary.averageDurationMs(),
                            summary.p95DurationMs()
                    );
                })
                .sorted(Comparator.comparing(ServiceSummaryResponse::serviceName))
                .toList();
    }

    @GetMapping("/timeseries")
    public List<TimeBucketResponse> timeseries(@RequestParam(name = "minutes", defaultValue = "60") int minutes) {
        return recent(minutes).stream()
                .collect(Collectors.groupingBy(metric -> new BucketKey(
                        metric.getTime().truncatedTo(ChronoUnit.MINUTES),
                        safe(metric.getServiceName())
                )))
                .entrySet()
                .stream()
                .map(entry -> new TimeBucketResponse(
                        entry.getKey().bucket(),
                        entry.getKey().serviceName(),
                        entry.getValue().size(),
                        entry.getValue().stream().filter(MetricEntity::isError).count(),
                        averageDuration(entry.getValue())
                ))
                .sorted(Comparator.comparing(TimeBucketResponse::bucket).thenComparing(TimeBucketResponse::serviceName))
                .toList();
    }

    @GetMapping("/recent")
    public List<MetricResponse> recent() {
        return metricRepository.findTop100ByOrderByTimeDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/traces/{traceId}")
    public List<MetricResponse> trace(@PathVariable String traceId) {
        return metricRepository.findByTraceIdOrderByTimeAsc(traceId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/web")
    public MetricResponse getWebMetrics(@RequestBody WebMetricRequest request, HttpServletRequest httpRequest) {
        return toResponse(metricRepository.save(toEntity(request, httpRequest)));
    }

    @PostMapping("/web/batch")
    public List<MetricResponse> getWebMetricsBatch(@RequestBody List<WebMetricRequest> requests,
                                                   HttpServletRequest httpRequest) {
        List<MetricEntity> entities = requests.stream()
                .map(request -> toEntity(request, httpRequest))
                .toList();
        return metricRepository.saveAll(entities).stream()
                .map(this::toResponse)
                .toList();
    }

    private List<MetricEntity> recent(int minutes) {
        return metricRepository.findByTimeAfterOrderByTimeDesc(LocalDateTime.now().minusMinutes(minutes));
    }

    private SummaryResponse summarize(List<MetricEntity> metrics) {
        long total = metrics.size();
        long errors = metrics.stream().filter(MetricEntity::isError).count();
        double errorRate = total == 0 ? 0.0 : errors * 100.0 / total;
        return new SummaryResponse(total, errors, errorRate, averageDuration(metrics), p95(metrics));
    }

    private double averageDuration(List<MetricEntity> metrics) {
        return metrics.stream().mapToInt(MetricEntity::getDurationMs).average().orElse(0.0);
    }

    private double p95(List<MetricEntity> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }
        List<Integer> values = metrics.stream()
                .map(MetricEntity::getDurationMs)
                .sorted()
                .toList();
        int index = Math.max(0, (int) Math.ceil(values.size() * 0.95) - 1);
        return values.get(index);
    }

    private MetricResponse toResponse(MetricEntity metric) {
        return new MetricResponse(
                metric.getId(),
                metric.getTime(),
                metric.getTraceId(),
                metric.getSpanId(),
                metric.getParentSpanId(),
                metric.getServiceName(),
                metric.getOperationType(),
                metric.getMethod(),
                metric.getEndpoint(),
                metric.getTopic(),
                metric.getDurationMs(),
                metric.getStatusCode(),
                metric.isError(),
                metric.getErrorMessage()
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private MetricEntity toEntity(WebMetricRequest request, HttpServletRequest httpRequest) {
        int statusCode = numberOrZero(request.statusCode());
        boolean isError = request.error() != null
                ? request.error()
                : statusCode >= 400 || present(request.errorMessage());

        MetricEntity entity = new MetricEntity();
        entity.setTime(parseTimeOrNow(request.time()));
        entity.setTraceId(valueOrGenerated(request.traceId(), httpRequest.getHeader(CustomHeaders.CORRELATION_ID_HEADER)));
        entity.setSpanId(valueOrGenerated(request.spanId(), httpRequest.getHeader(CustomHeaders.SPAN_ID_HEADER)));
        entity.setParentSpanId(blankToNull(request.parentSpanId()));
        entity.setServiceName(valueOrDefault(request.serviceName(), "web-frontend"));
        entity.setOperationType(valueOrDefault(request.operationType(), "FRONTEND"));
        entity.setMethod(blankToNull(request.method()));
        entity.setEndpoint(valueOrDefault(request.endpoint(), httpRequest.getHeader("Referer")));
        entity.setTopic(blankToNull(request.topic()));
        entity.setDurationMs(clampInt(request.durationMs()));
        entity.setStatusCode(statusCode);
        entity.setError(isError);
        entity.setErrorMessage(trimToLimit(request.errorMessage(), 2000));
        return entity;
    }

    private LocalDateTime parseTimeOrNow(String value) {
        if (!present(value)) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ignoredAgain) {
                return LocalDateTime.now();
            }
        }
    }

    private int numberOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int clampInt(Integer value) {
        if (value == null || value < 0) {
            return 0;
        }
        return value;
    }

    private String valueOrGenerated(String first, String second) {
        if (present(first)) {
            return first;
        }
        if (present(second)) {
            return second;
        }
        return UUID.randomUUID().toString();
    }

    private String valueOrDefault(String value, String fallback) {
        if (present(value)) {
            return value;
        }
        return present(fallback) ? fallback : "unknown";
    }

    private String blankToNull(String value) {
        return present(value) ? value : null;
    }

    private boolean present(String value) {
        return value != null && !value.isBlank();
    }

    private String trimToLimit(String value, int limit) {
        if (value == null || value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit);
    }

    private record BucketKey(LocalDateTime bucket, String serviceName) {
    }

    public record WebMetricRequest(
            String time,
            String traceId,
            String spanId,
            String parentSpanId,
            String serviceName,
            String operationType,
            String method,
            String endpoint,
            String topic,
            Integer durationMs,
            Integer statusCode,
            @JsonAlias("isError") Boolean error,
            String errorMessage
    ) {
    }
}
