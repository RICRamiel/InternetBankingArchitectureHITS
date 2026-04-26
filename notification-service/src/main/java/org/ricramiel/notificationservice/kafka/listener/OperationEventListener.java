package org.ricramiel.notificationservice.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.tracing.MonitoringEventPublisher;
import org.ricramiel.common.tracing.TraceContext;
import org.ricramiel.common.tracing.TraceHeaders;
import org.ricramiel.common.util.ChaosUtil;
import org.ricramiel.notificationservice.dto.OperationPayload;
import org.ricramiel.notificationservice.entity.Notification;
import org.ricramiel.notificationservice.service.FcmService;
import org.ricramiel.notificationservice.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationEventListener {
    private final NotificationService notificationService;
    private final FcmService fcmService;
    private final MonitoringEventPublisher monitoringEventPublisher;

    @KafkaListener(topicPattern = "${app.kafka.topics.consumer.enroll}|${app.kafka.topics.consumer.withdraw}", groupId = "notification-service-group")
    public void handleTransactionEvent(EventTransactionDto event, Acknowledgment ack) {
        long start = System.currentTimeMillis();
        TraceContext.TraceState trace = TraceHeaders.openFrom(event);
        boolean error = false;
        String errorMessage = null;

        try {
            ChaosUtil.simulateKafkaProcessingError();

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
            log.info("Received event {}", event.getId());

            OperationPayload payload = OperationPayload.builder()
                    .operationId(data.getId())
                    .type(data.getTransactionType().name())
                    .amount(data.getMoney())
                    .currency(data.getCurrency())
                    .message(data.getAction())
                    .build();

            Notification savedNotification = notificationService.saveToHistoryAndGetUserId(event.getId(), data.getAccountId(), payload);
            UUID clientId = savedNotification.getUserId();

            fcmService.sendToUser(clientId, savedNotification, "WEB_CLIENT");
            fcmService.sendToAllWorkers(savedNotification);

            log.info("Successfully processed and triggered FCM push for operation: {}", data.getId());
            ack.acknowledge();
        } catch (Exception e) {
            error = true;
            errorMessage = e.getMessage();
            log.error("Error processing transaction event {}: {}", event.getId(), e.getMessage(), e);
        } finally {
            monitoringEventPublisher.publish(monitoringEventPublisher.metric(
                    trace,
                    MonitoringEventPublisher.KAFKA_CONSUMER,
                    "CONSUME",
                    null,
                    event.getDestination(),
                    System.currentTimeMillis() - start,
                    error ? 500 : 200,
                    error,
                    errorMessage
            ));
            TraceContext.clear();
        }
    }
}
