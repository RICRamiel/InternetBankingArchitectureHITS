package org.ricramiel.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventMetricDto {
    private LocalDateTime time;
    private String traceId;
    private String serviceName;
    private String endpoint;
    private int durationMs;
    private int statusCode;
    private boolean isError;
}