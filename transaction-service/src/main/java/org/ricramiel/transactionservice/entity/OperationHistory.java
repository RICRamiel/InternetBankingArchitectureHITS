package org.ricramiel.transactionservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationHistory {

    //sourceId in transactionDTO
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private BigDecimal sum;

    private String destination;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    private UUID cardAccountId;
}
