package org.ricramiel.coreapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventAccountCreate;
import org.ricramiel.common.enums.OutboxStatus;
import org.ricramiel.common.tracing.MonitoringEventPublisher;
import org.ricramiel.common.tracing.TraceContext;
import org.ricramiel.common.tracing.TraceHeaders;
import org.ricramiel.coreapi.entity.OutboxAccountEvent;
import org.ricramiel.coreapi.repository.OutboxAccountEventRepository;
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
public class KafkaEventAccountService {
    private final KafkaTemplate<String, EventAccountCreate> template;
    private final OutboxAccountEventRepository outboxAccountEventRepository;
    private final ObjectMapper  objectMapper;
    private final MonitoringEventPublisher monitoringEventPublisher;

    @Scheduled(fixedRateString = "${outbox.scheduled}")
    public void eventProcessing() {
        List<OutboxAccountEvent> listOfOutboxEventEntities = new ArrayList<>(outboxAccountEventRepository.findAllbyStatus("PENDING"));

        log.info("Number of outbox events: {}", listOfOutboxEventEntities.size());

        if (!listOfOutboxEventEntities.isEmpty()) {
            for (OutboxAccountEvent outboxEvent : listOfOutboxEventEntities) {
                log.info("Sending event to Kafka");
                outboxEvent.setStatus(OutboxStatus.SEND);
                if (sendToKafka(outboxEvent)) {
                    outboxAccountEventRepository.save(outboxEvent);
                }
            }
        }
    }

    private boolean sendToKafka(OutboxAccountEvent outboxEventEntity) {
        long start = System.currentTimeMillis();
        TraceContext.TraceState trace = TraceContext.currentOrNew();
        try {
            EventAccountCreate event = objectMapper.readValue(outboxEventEntity.getPayload(), EventAccountCreate.class);
            trace = TraceHeaders.openFrom(event);
            event.setParentSpanId(trace.spanId());
            CompletableFuture<SendResult<String, EventAccountCreate>> sendResult = template.send(
                    outboxEventEntity.getOutboxTopic(),
                    event);
            SendResult<String, EventAccountCreate> result = sendResult.get();
            log.info("Partition: {}", result.getRecordMetadata().partition());
            monitoringEventPublisher.publish(monitoringEventPublisher.metric(
                    trace, MonitoringEventPublisher.KAFKA_PRODUCER, "SEND", null,
                    outboxEventEntity.getOutboxTopic(), System.currentTimeMillis() - start,
                    200, false, null));
            return true;
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Error sending event to Kafka: {}", e.getMessage());
            monitoringEventPublisher.publish(monitoringEventPublisher.metric(
                    trace, MonitoringEventPublisher.KAFKA_PRODUCER, "SEND", null,
                    outboxEventEntity.getOutboxTopic(), System.currentTimeMillis() - start,
                    500, true, e.getMessage()));
            return false;
        } finally {
            TraceContext.clear();
        }
    }
}
