package org.ricramiel.monitoringservice.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.monitoringservice.dto.MetricResponse;
import org.ricramiel.monitoringservice.dto.ServiceSummaryResponse;
import org.ricramiel.monitoringservice.dto.SummaryResponse;
import org.ricramiel.monitoringservice.dto.TimeBucketResponse;
import org.ricramiel.monitoringservice.entity.MetricEntity;
import org.ricramiel.monitoringservice.repository.MetricRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsController {
    private final MetricRepository metricRepository;

    @GetMapping("/summary")
    public SummaryResponse summary(@RequestParam(defaultValue = "60") int minutes) {
        return summarize(recent(minutes));
    }

    @GetMapping("/services")
    public List<ServiceSummaryResponse> services(@RequestParam(defaultValue = "60") int minutes) {
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
    public List<TimeBucketResponse> timeseries(@RequestParam(defaultValue = "60") int minutes) {
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

    private record BucketKey(LocalDateTime bucket, String serviceName) {
    }
}
