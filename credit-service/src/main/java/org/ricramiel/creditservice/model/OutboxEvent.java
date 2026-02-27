package org.ricramiel.creditservice.model;

import jakarta.persistence.*;
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
