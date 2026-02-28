package org.ricramiel.common.dtos;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@NoArgsConstructor
public class EventWithdrawDto extends EventDto<WithdrawDto> {
    public EventWithdrawDto(UUID id, WithdrawDto data, LocalDateTime creationDate, String type) {
        super(id, data, creationDate, type);
    }
}
