package org.ricramiel.common.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EventDto<T>{
    private UUID id;
    private T data;
    private LocalDateTime creationDate;
    private String type;
}
