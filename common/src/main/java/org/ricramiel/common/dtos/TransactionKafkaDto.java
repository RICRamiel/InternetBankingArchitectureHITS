package org.ricramiel.common.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Data
public class TransactionKafkaDto {
    //nullable
    private UUID id;

    private UUID sourceId;

    private UUID accountId;

    private LocalDateTime dateTime;

    private TransactionType transactionType;

    private TransactionStatus transactionStatus;

    private String action;

    private BigDecimal money;

    public TransactionKafkaDto(UUID sourceId,
                               UUID accountId,
                               LocalDateTime dateTime,
                               TransactionType transactionType,
                               TransactionStatus transactionStatus,
                               String action,
                               BigDecimal money) {
        this.sourceId = sourceId;
        this.accountId = accountId;
        this.dateTime = dateTime;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.action = action;
        this.money = money;
    }
}
