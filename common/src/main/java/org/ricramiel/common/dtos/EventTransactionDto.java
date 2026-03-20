package org.ricramiel.common.dtos;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@NoArgsConstructor
public class EventTransactionDto extends EventDto<TransactionKafkaDto> {
    public EventTransactionDto(UUID id, TransactionKafkaDto data, LocalDateTime creationDate, String type) {
        super(id, data, creationDate, type);
    }
}
