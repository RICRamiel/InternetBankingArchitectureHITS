package org.ricramiel.coreapi.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.EventWithdrawDto;
import org.ricramiel.common.enums.OutboxStatus;
import org.ricramiel.coreapi.entity.OutboxEvent;
import org.ricramiel.coreapi.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventWithdrawService {
    private final KafkaTemplate<String, EventTransactionDto> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    @Scheduled(fixedRateString = "${outbox.scheduled}")
    public void eventProcessing() {
        List<OutboxEvent> listOfOutboxEventEntities = new ArrayList<>(outboxRepository.findAllbyStatus("PENDING"));

        log.info("Number of outbox events: {}", listOfOutboxEventEntities.size());

        if (!listOfOutboxEventEntities.isEmpty()) {
            for (OutboxEvent outboxEvent : listOfOutboxEventEntities) {
                log.info("Sending event to Kafka");
                outboxEvent.setStatus(OutboxStatus.SEND);
                outboxRepository.save(outboxEvent);
                sendToKafka(outboxEvent);
//                outboxRepository.deleteById(outboxEvent.getId());
            }
        }
    }

    private void sendToKafka(OutboxEvent outboxEventEntity) {
        try {
            CompletableFuture<SendResult<String, EventTransactionDto>> sendResult = kafkaTemplate.send(
                    outboxEventEntity.getOutboxTopic(),
                    objectMapper.readValue(outboxEventEntity.getPayload(), EventTransactionDto.class));
            SendResult<String, EventTransactionDto> result = sendResult.get();
            log.info("Partition: {}", result.getRecordMetadata().partition());
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            log.error("Error sending event to Kafka: {}", e.getMessage());
        }
    }
}
