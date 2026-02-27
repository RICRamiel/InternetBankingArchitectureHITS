package org.ricramiel.creditservice.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.EventEnrollDto;
import org.ricramiel.common.enums.OutboxStatus;
import org.ricramiel.creditservice.model.OutboxEvent;
import org.ricramiel.creditservice.repository.OutboxRepository;
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
public class CreditCreateProducer {
    private static final Logger LOG = LoggerFactory.getLogger(CreditCreateProducer.class);
    private final KafkaTemplate<String, EventEnrollDto> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    @Scheduled(fixedRateString = "${outbox.scheduled}")
    public void eventProcessing() {
        List<OutboxEvent> listOfOutboxEventEntities = new ArrayList<>(outboxRepository.findAll());

        LOG.info("Number of outbox events: {}", listOfOutboxEventEntities.size());

        if (!listOfOutboxEventEntities.isEmpty()) {
            for (OutboxEvent outboxEvent : listOfOutboxEventEntities) {
                LOG.info("Sending event to Kafka");
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
            LOG.info("Partition: {}", result.getRecordMetadata().partition());
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            LOG.error("Error sending event to Kafka: {}", e.getMessage());
        }
    }
}

