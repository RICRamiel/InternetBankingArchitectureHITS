package org.ricramiel.creditservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "payment_history_record")
public class PaymentHistoryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private BigDecimal sum;
    private LocalDateTime date;
    private UUID userId;
    private UUID cardAccount;
}
