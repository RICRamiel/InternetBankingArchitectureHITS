package org.ricramiel.creditservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Table(name = "credit_rating")
public class CreditRating {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private BigDecimal rating;
}
