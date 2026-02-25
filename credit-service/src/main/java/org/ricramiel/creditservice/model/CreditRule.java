package org.ricramiel.creditservice.model;

import com.google.type.DateTime;
import jakarta.persistence.*;
import lombok.Data;
import org.ricramiel.creditservice.enums.PercentageStrategy;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "credit_rule")
public class CreditRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Enumerated
    private PercentageStrategy percentageStrategy;

    private DateTime collectionPeriod;
    private DateTime openingDate;
    private String ruleName;
    private BigDecimal percentage;
}
