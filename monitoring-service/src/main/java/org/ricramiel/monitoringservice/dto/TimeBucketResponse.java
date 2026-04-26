package org.ricramiel.monitoringservice.dto;

import java.time.LocalDateTime;

public record TimeBucketResponse(
        LocalDateTime bucket,
        String serviceName,
        long total,
        long errors,
        double averageDurationMs
) {
}
