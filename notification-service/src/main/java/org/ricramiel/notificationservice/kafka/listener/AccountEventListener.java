package org.ricramiel.notificationservice.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventAccountCreate;
import org.ricramiel.common.tracing.MonitoringEventPublisher;
import org.ricramiel.common.tracing.TraceContext;
import org.ricramiel.common.tracing.TraceHeaders;
import org.ricramiel.common.util.ChaosUtil;
import org.ricramiel.notificationservice.entity.AccountUserMapping;
import org.ricramiel.notificationservice.repository.AccountUserMappingRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventListener {

    private final AccountUserMappingRepository mappingRepository;
    private final MonitoringEventPublisher monitoringEventPublisher;

    @KafkaListener(topics = "account-event", groupId = "notification-service-group")
    public void handleAccountCreation(EventAccountCreate event, Acknowledgment ack) {
        long start = System.currentTimeMillis();
        TraceContext.TraceState trace = TraceHeaders.openFrom(event);
        boolean error = false;
        String errorMessage = null;

        try {
            //ChaosUtil.simulateKafkaProcessingError();

            AccountUserMapping mapping = AccountUserMapping.builder()
                    .accountId(event.getCardAccountId())
                    .userId(event.getUserId())
                    .build();

            mappingRepository.save(mapping);
            log.info("Saved account-user mapping: {} -> {}", event.getCardAccountId(), event.getUserId());
            ack.acknowledge();
        } catch (Exception e) {
            error = true;
            errorMessage = e.getMessage();
            log.warn("Failed to save account-user mapping for account {}: {}", event.getCardAccountId(), e.getMessage(), e);
        } finally {
            monitoringEventPublisher.publish(monitoringEventPublisher.metric(
                    trace,
                    MonitoringEventPublisher.KAFKA_CONSUMER,
                    "CONSUME",
                    null,
                    "account-event",
                    System.currentTimeMillis() - start,
                    error ? 500 : 200,
                    error,
                    errorMessage
            ));
            TraceContext.clear();
        }
    }
}
