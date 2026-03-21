package org.ricramiel.coreapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {
    private UUID fromCardAccountId;
    private UUID toCardAccountId;
    private BigDecimal sum;
    private String currency;
}
