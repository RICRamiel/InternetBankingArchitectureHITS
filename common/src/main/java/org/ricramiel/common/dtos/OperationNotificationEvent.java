package org.ricramiel.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ricramiel.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperationNotificationEvent {
    private UUID operationId;
    private UUID clientId;
    private String accountName;
    private BigDecimal amount;
    private TransactionType type;
    private Instant createdAt;
}