package org.ricramiel.common.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public class EventWithdrawDto extends EventDto<WithdrawDto> {
    public EventWithdrawDto(UUID id, WithdrawDto data, LocalDateTime creationDate, String type) {
        super(id, data, creationDate, type);
    }
}
