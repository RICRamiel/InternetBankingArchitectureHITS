package org.ricramiel.coreapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CurrencyConvertRequestDto {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
}
