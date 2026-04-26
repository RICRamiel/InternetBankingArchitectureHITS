package org.ricramiel.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventAccountCreate {
    private UUID cardAccountId;
    private UUID userId;
    private String traceId;
    private String parentSpanId;

    public EventAccountCreate(UUID cardAccountId, UUID userId) {
        this.cardAccountId = cardAccountId;
        this.userId = userId;
    }
}
