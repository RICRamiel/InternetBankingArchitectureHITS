package org.ricramiel.coreapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ricramiel.common.enums.OutboxStatus;

@Entity
@Data
@NoArgsConstructor
@Table(name = "outbox")
public class OutboxEvent {
    @Id
    @GeneratedValue
    private int id;
    @Size(min = 1, max = 5000)
    @NotBlank
    private String payload;
    @Enumerated(EnumType.STRING)
    private OutboxStatus status = OutboxStatus.PENDING;
    @NotBlank
    private String outboxTopic;
}
