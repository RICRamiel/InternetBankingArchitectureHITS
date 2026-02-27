package org.ricramiel.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDto<T>{
    private UUID id;
    private T data;
    private LocalDateTime creationDate;
    private String type;
}
