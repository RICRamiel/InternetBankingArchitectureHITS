package org.ricramiel.creditservice.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.enums.OutboxStatus;
import org.ricramiel.common.tracing.MonitoringEventPublisher;
import org.ricramiel.common.tracing.TraceContext;
import org.ricramiel.common.tracing.TraceHeaders;
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
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class CreditCreateProducer {
    private static final Logger LOG = LoggerFactory.getLogger(CreditCreateProducer.class);
    private final KafkaTemplate<String, EventTransactionDto> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final MonitoringEventPublisher monitoringEventPublisher;


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
        long start = System.currentTimeMillis();
        TraceContext.TraceState trace = TraceContext.currentOrNew();
        try {
            EventTransactionDto event = objectMapper.readValue(outboxEventEntity.getPayload(), EventTransactionDto.class);
            trace = TraceHeaders.openFrom(event);
            event.setParentSpanId(trace.spanId());
            var sendResult =
            kafkaTemplate.send(
                    outboxEventEntity.getOutboxTopic(),
                    event);
            SendResult<String, EventTransactionDto> result = sendResult.get();
            LOG.info("Partition: {}", result.getRecordMetadata().partition());
            monitoringEventPublisher.publish(monitoringEventPublisher.metric(
                    trace, MonitoringEventPublisher.KAFKA_PRODUCER, "SEND", null,
                    outboxEventEntity.getOutboxTopic(), System.currentTimeMillis() - start,
                    200, false, null));
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOG.error("Error sending event to Kafka: {}", e.getMessage());
            monitoringEventPublisher.publish(monitoringEventPublisher.metric(
                    trace, MonitoringEventPublisher.KAFKA_PRODUCER, "SEND", null,
                    outboxEventEntity.getOutboxTopic(), System.currentTimeMillis() - start,
                    500, true, e.getMessage()));
            throw new RuntimeException();
        } finally {
            TraceContext.clear();
        }
    }
}

