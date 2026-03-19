package org.ricramiel.coreapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    private String name;
    //нужно чтобы типа как по СБП по номеру телефона
    private Boolean isMain;

    private UUID userId;

    private BigDecimal money;

    private Boolean deleted;
}
