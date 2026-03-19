package org.ricramiel.creditservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditRuleAnswerDTO {
    private UUID id;
    private Integer collectionPeriodSeconds;
    private LocalDateTime openingDate;
    private String ruleName;
    private BigDecimal percentage;
}
