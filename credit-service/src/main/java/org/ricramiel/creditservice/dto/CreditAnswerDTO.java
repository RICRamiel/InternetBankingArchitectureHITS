package org.ricramiel.creditservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditAnswerDTO {
    private UUID id;
    private LocalDateTime lastInterestUpdate;
    private UUID userId;
    private UUID cardAccount;
    private BigDecimal currentDebtSum;
    private BigDecimal initialDebt;
    private BigDecimal interestDebtSum;
    private CreditRuleAnswerDTO creditRule;
}
