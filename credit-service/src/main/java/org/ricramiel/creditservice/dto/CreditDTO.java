package org.ricramiel.creditservice.dto;

import lombok.Data;
import org.ricramiel.creditservice.model.CreditRule;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreditDTO {
    private UUID userId;
    private UUID cardAccount;
    private BigDecimal totalDebt;
    private CreditRule creditRule;
}
