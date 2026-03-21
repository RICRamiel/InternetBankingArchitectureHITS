package org.ricramiel.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferDto {
    private UUID fromCardAccountId;
    private UUID toCardAccountId;
    private BigDecimal sum;
    private String currency;
}
