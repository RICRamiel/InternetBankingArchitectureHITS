package org.ricramiel.monitoringservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricEntity {

    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "span_id")
    private String spanId;

    @Column(name = "parent_span_id")
    private String parentSpanId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "method")
    private String method;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "topic")
    private String topic;

    @Column(name = "duration_ms")
    private int durationMs;

    @Column(name = "status_code")
    private int statusCode;

    @Column(name = "is_error")
    private boolean isError;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}
