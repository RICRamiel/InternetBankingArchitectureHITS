package org.ricramiel.coreapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Entity
@Data
@Table(name = "cardAccount")
public class CardAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    private UUID userId;

    private BigDecimal money;

    private Boolean deleted;

    @OneToMany(fetch = FetchType.LAZY)
    private List<TransactionOperation> transactionOperations;
}
