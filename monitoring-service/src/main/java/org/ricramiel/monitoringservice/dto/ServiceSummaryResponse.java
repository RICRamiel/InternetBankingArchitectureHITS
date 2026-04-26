package org.ricramiel.monitoringservice.dto;

public record ServiceSummaryResponse(
        String serviceName,
        long total,
        long errors,
        double errorRate,
        double averageDurationMs,
        double p95DurationMs
) {
}
