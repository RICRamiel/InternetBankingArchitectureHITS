package org.ricramiel.creditservice.dto;

import lombok.Data;
import org.ricramiel.common.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentHistoryRecordDTO {
    private UUID id;
    private BigDecimal sum;
    private LocalDateTime date;
    private UUID userId;
    private UUID cardAccount;
    private String currency;
    private TransactionStatus transactionStatus;
}
