package org.ricramiel.coreapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Lock;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Entity
@Data
@Table(name = "cardAccount")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardAccount {
    @Id
    @GeneratedValue()
    private UUID id;

    private UUID userId;

    private BigDecimal money;

    private Boolean deleted;

    @OneToMany(fetch = FetchType.LAZY)
    private List<TransactionOperation> transactionOperations;
}
