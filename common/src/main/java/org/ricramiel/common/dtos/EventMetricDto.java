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
    private String spanId;
    private String parentSpanId;
    private String serviceName;
    private String operationType;
    private String method;
    private String endpoint;
    private String topic;
    private int durationMs;
    private int statusCode;
    private boolean isError;
    private String errorMessage;
}
