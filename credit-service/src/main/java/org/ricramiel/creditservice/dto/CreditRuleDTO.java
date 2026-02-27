package org.ricramiel.creditservice.dto;

import lombok.Data;
import org.ricramiel.creditservice.enums.PercentageStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreditRuleDTO {
    private PercentageStrategy percentageStrategy;
    private Integer collectionPeriodSeconds;
    private LocalDateTime openingDate;
    private String ruleName;
    private BigDecimal percentage;
}
