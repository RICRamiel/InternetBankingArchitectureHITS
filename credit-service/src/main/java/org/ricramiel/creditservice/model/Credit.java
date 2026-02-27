package org.ricramiel.creditservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit")
public class Credit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private UUID cardAccount;
    private BigDecimal debt;
    private BigDecimal totalDebt;

    @ManyToOne
    @JoinColumn(name = "credit_rule_id")
    private CreditRule creditRule;
}
