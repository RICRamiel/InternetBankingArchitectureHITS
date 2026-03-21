package org.ricramiel.creditservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditCreateModelDto {
    private UUID userId;
    private UUID cardAccount;
    private BigDecimal totalDebt;
    private UUID creditRuleId;
}