package org.ricramiel.coreapi.entity;

import com.google.type.DateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transactionOperation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private CardAccount account;

    private DateTime dateTime;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    private BigDecimal money;
}
