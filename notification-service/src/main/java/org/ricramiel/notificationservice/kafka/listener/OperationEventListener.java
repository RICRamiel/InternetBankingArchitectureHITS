package org.ricramiel.notificationservice.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.notificationservice.dto.SseOperationPayload;
import org.ricramiel.notificationservice.service.NotificationService;
import org.ricramiel.notificationservice.service.SseEmitterStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationEventListener {

    private final SseEmitterStore emitterStore;
    private final NotificationService notificationService;

    @KafkaListener(topicPattern = "${app.kafka.topics.consumer.enroll}|${app.kafka.topics.consumer.withdraw}", groupId = "notification-service-group")
    public void handleTransactionEvent(EventTransactionDto event, Acknowledgment ack) {
        TransactionKafkaDto data = event.getData();
        if (data == null) {
            log.warn("Received event {} with null data", event.getId());
            return;
        }

        if (data.getTransactionStatus() != TransactionStatus.COMPLETE) {
            return;
        }
        if (notificationService.isEventAlreadyProcessed(event.getId())) {
            log.debug("Event {} already processed, skipping", event.getId());
            return;
        }

        try {
            SseOperationPayload payload = SseOperationPayload.builder()
                    .operationId(data.getId())
                    .type(data.getTransactionType().name())
                    .amount(data.getMoney())
                    .currency(data.getCurrency())
                    .message(data.getAction())
                    .build();

            UUID clientId = notificationService.saveToHistoryAndGetUserId(event.getId(), data.getAccountId(), payload);
            emitterStore.sendToClient(clientId, payload);
            emitterStore.sendToAllEmployees(payload);

            log.info("Successfully processed and sent notification for operation: {}", data.getId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing transaction event {}: {}", event.getId(), e.getMessage(), e);
        }
    }
}