package org.ricramiel.monitoringservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MetricResponse(
        UUID id,
        LocalDateTime time,
        String traceId,
        String spanId,
        String parentSpanId,
        String serviceName,
        String operationType,
        String method,
        String endpoint,
        String topic,
        int durationMs,
        int statusCode,
        boolean error,
        String errorMessage
) {
}
