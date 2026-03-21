package org.ricramiel.coreapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private Boolean isMain;
    //Валюта счета
    //нужна проверка на перевод
    private String currency;

    private UUID userId;

    private BigDecimal money;

    private Boolean deleted;
}
