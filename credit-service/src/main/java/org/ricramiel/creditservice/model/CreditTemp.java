package org.ricramiel.creditservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_temp")
public class CreditTemp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime lastInterestUpdate;
    private UUID userId;
    private UUID cardAccount;
    private String currency;
    private BigDecimal currentDebtSum;
    private BigDecimal initialDebt;
    private BigDecimal interestDebtSum;

    @ManyToOne
    @JoinColumn(name = "credit_rule_id")
    private CreditRule creditRule;

}
