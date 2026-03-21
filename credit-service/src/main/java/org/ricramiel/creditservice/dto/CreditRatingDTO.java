package org.ricramiel.creditservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreditRatingDTO {
    private UUID id;
    private UUID userId;
    private BigDecimal rating;
}
