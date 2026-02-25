package org.ricramiel.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WithdrawDto {
    private UUID cardAccountId;
    private BigDecimal sum;
    private String destination;
}
