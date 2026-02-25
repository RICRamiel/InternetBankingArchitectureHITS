package org.ricramiel.creditservice.dto;

import com.google.type.DateTime;
import lombok.Data;
import org.ricramiel.creditservice.enums.PercentageStrategy;

import java.math.BigDecimal;

@Data
public class CreditRuleDTO {
    private PercentageStrategy percentageStrategy;
    private DateTime collectionPeriod;
    private DateTime openingDate;
    private String ruleName;
    private BigDecimal percentage;
}
