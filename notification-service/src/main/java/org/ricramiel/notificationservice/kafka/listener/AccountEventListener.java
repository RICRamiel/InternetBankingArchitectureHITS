package org.ricramiel.notificationservice.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventAccountCreate;
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

    @KafkaListener(topics = "account-event", groupId = "notification-service-group")
    public void handleAccountCreation(EventAccountCreate event, Acknowledgment ack) {
        //Имитируем проблему кафки
        ChaosUtil.simulateKafkaProcessingError();

        AccountUserMapping mapping = AccountUserMapping.builder()
                .accountId(event.getCardAccountId())
                .userId(event.getUserId())
                .build();

        try {
            mappingRepository.save(mapping);
            log.info("Saved account-user mapping: {} -> {}", event.getCardAccountId(), event.getUserId());
            ack.acknowledge();
        } catch (Exception e) {
            log.warn("Mapping already exists for account: {}", event.getCardAccountId());
        }
    }
}