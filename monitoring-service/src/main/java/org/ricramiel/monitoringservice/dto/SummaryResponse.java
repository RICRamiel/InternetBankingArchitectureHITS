package org.ricramiel.monitoringservice.dto;

public record SummaryResponse(
        long total,
        long errors,
        double errorRate,
        double averageDurationMs,
        double p95DurationMs
) {
}
