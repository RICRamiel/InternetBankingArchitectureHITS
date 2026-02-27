package org.ricramiel.common.dtos;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
public class EventEnrollDto extends EventDto<EnrollDto>{
    public EventEnrollDto(UUID id, EnrollDto data, LocalDateTime creationDate, String type){
        super(id, data, creationDate, type);
    }
}
