package org.ricramiel.common.tracing;

import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventMetricDto;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MonitoringEventPublisher {
    public static final String HTTP_SERVER = "HTTP_SERVER";
    public static final String GATEWAY = "GATEWAY";
    public static final String KAFKA_PRODUCER = "KAFKA_PRODUCER";
    public static final String KAFKA_CONSUMER = "KAFKA_CONSUMER";

    private final ObjectProvider<KafkaTemplate> kafkaTemplateProvider;

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    @Value("${monitoring.metrics-topic:metrics-topic}")
    private String metricsTopic;

    public MonitoringEventPublisher(
            ObjectProvider<KafkaTemplate> kafkaTemplateProvider
    ) {
        this.kafkaTemplateProvider = kafkaTemplateProvider;
    }

    public void publish(EventMetricDto metric) {
        if (metric.getServiceName() == null) {
            metric.setServiceName(serviceName);
        }
        if (metric.getTime() == null) {
            metric.setTime(LocalDateTime.now());
        }

        KafkaTemplate kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            log.debug("Monitoring KafkaTemplate is not available, metric skipped: {}", metric);
            return;
        }

        try {
            kafkaTemplate.send(metricsTopic, metric.getTraceId(), metric)
                    .exceptionally(error -> {
                        log.debug("Failed to send monitoring metric: {}", errorMessage(error));
                        return null;
                    });
        } catch (Exception e) {
            log.debug("Failed to enqueue monitoring metric: {}", e.getMessage());
        }
    }

    public EventMetricDto metric(
            TraceContext.TraceState trace,
            String operationType,
            String method,
            String endpoint,
            String topic,
            long durationMs,
            int statusCode,
            boolean isError,
            String errorMessage
    ) {
        return EventMetricDto.builder()
                .time(LocalDateTime.now())
                .traceId(trace.traceId())
                .spanId(trace.spanId())
                .parentSpanId(trace.parentSpanId())
                .serviceName(serviceName)
                .operationType(operationType)
                .method(method)
                .endpoint(endpoint)
                .topic(topic)
                .durationMs((int) Math.min(durationMs, Integer.MAX_VALUE))
                .statusCode(statusCode)
                .isError(isError)
                .errorMessage(errorMessage)
                .build();
    }

    private String errorMessage(Object error) {
        if (error instanceof Throwable throwable) {
            return throwable.getMessage();
        }
        return String.valueOf(error);
    }
}
