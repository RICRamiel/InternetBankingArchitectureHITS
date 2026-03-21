package org.ricramiel.creditservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ricramiel.common.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@Table(name = "payment_history_record")
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private BigDecimal sum;
    private LocalDateTime date;
    private String currency;
    private UUID cardAccount;
    private TransactionStatus transactionStatus;
}
