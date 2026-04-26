package org.ricramiel.common.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class EventTransactionDto{
    private UUID id;
    private TransactionKafkaDto data;
    private LocalDateTime creationDate;
    private String destination;
    private String traceId;
    private String parentSpanId;

    public EventTransactionDto(UUID id, TransactionKafkaDto data, LocalDateTime creationDate, String destination) {
        this.id = id;
        this.data = data;
        this.creationDate = creationDate;
        this.destination = destination;
    }
}
