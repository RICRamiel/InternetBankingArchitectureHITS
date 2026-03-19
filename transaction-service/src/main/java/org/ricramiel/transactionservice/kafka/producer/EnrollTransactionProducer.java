package org.ricramiel.transactionservice.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventEnrollDto;
import org.ricramiel.common.dtos.EventWithdrawDto;
import org.ricramiel.common.enums.OutboxStatus;
import org.ricramiel.transactionservice.entity.OutboxEvent;
import org.ricramiel.transactionservice.repository.OutboxRepository;
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
public class EnrollTransactionProducer {
    private final KafkaTemplate<String, EventEnrollDto> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRateString = "${outbox.scheduled}")
    public void eventProcessing() {
        List<OutboxEvent> listOfOutboxEventEntities = new ArrayList<>(outboxRepository.findAll());

        log.debug("Number of outbox events: {}", listOfOutboxEventEntities.size());

        if (!listOfOutboxEventEntities.isEmpty()) {
            for (OutboxEvent outboxEvent : listOfOutboxEventEntities) {
                log.debug("Sending event to Kafka");
                outboxEvent.setStatus(OutboxStatus.SEND);
                sendToKafka(outboxEvent);
                outboxRepository.deleteById(outboxEvent.getId());
            }
        }
    }

    private void sendToKafka(OutboxEvent outboxEventEntity) {
        try {
            CompletableFuture<SendResult<String, EventEnrollDto>> sendResult = kafkaTemplate.send(
                    outboxEventEntity.getOutboxTopic(),
                    objectMapper.readValue(outboxEventEntity.getPayload(), EventEnrollDto.class));
            SendResult<String, EventEnrollDto> result = sendResult.get();
            log.info("Partition: {}", result.getRecordMetadata().partition());
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            log.error("Error sending event to Kafka: {}", e.getMessage());
        }
    }
}
