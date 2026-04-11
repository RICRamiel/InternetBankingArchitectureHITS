package org.ricramiel.notificationservice.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SseOperationPayload {
    private UUID operationId;
    private String type;
    private BigDecimal amount;
    private String currency;
    private String message;
}