package org.ricramiel.creditservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreditRuleDTO {
    private Integer collectionPeriodSeconds;
    private LocalDateTime openingDate;
    private String ruleName;
    private BigDecimal percentage;
}
