package org.ricramiel.creditservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.ricramiel.creditservice.enums.PercentageStrategy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "credit_rule")
public class CreditRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated
    private PercentageStrategy percentageStrategy;

    private Duration collectionPeriod;
    private LocalDateTime openingDate;
    private String ruleName;
    private BigDecimal percentage;
}
