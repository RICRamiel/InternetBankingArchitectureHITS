package org.ricramiel.coreapi.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;
import org.ricramiel.common.tracing.MonitoringEventPublisher;
import org.ricramiel.common.tracing.TraceContext;
import org.ricramiel.common.tracing.TraceHeaders;
import org.ricramiel.common.util.ChaosUtil;
import org.ricramiel.coreapi.model.IdempotencyKey;
import org.ricramiel.coreapi.repository.IdempotencyKeyRepository;
import org.ricramiel.coreapi.service.ExternalTransactionsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionListener {

    private final ExternalTransactionsService transactionsService;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final MonitoringEventPublisher monitoringEventPublisher;

    @KafkaListener(topicPattern = "${app.kafka.topics.consumer.enroll}|${app.kafka.topics.consumer.withdraw}", groupId = "transaction")
    public void listenWithAck(@Payload EventTransactionDto eventTransactionDto, Acknowledgment acknowledgment) {
        long start = System.currentTimeMillis();
        TraceContext.TraceState trace = TraceHeaders.openFrom(eventTransactionDto);
        boolean error = false;
        String errorMessage = null;

        try {
            ChaosUtil.simulateKafkaProcessingError();
            if (!idempotencyKeyRepository.existsById(eventTransactionDto.getId())) {
                TransactionKafkaDto dto = eventTransactionDto.getData();
                log.info("transaction listener received data with destination: {}", dto.getAction());
                if (Objects.equals(dto.getTransactionStatus(), TransactionStatus.IN_PROGRESS)) {
                    if (Objects.equals(dto.getTransactionType(), TransactionType.ENROLLMENT)) {
                        log.info(String.valueOf(!Objects.equals(eventTransactionDto.getDestination(), "client")));
                        log.info(eventTransactionDto.toString());
                        transactionsService.enroll(dto, !Objects.equals(eventTransactionDto.getDestination(), "client"), eventTransactionDto.getDestination());
                    }
                    if (Objects.equals(dto.getTransactionType(), TransactionType.WITHDRAWAL)) {
                        transactionsService.withdraw(dto, !Objects.equals(eventTransactionDto.getDestination(), "client"), eventTransactionDto.getDestination());
                    }
                }

                IdempotencyKey idempotencyKey = IdempotencyKey.builder().id(eventTransactionDto.getId()).build();
                idempotencyKeyRepository.save(idempotencyKey);

                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            error = true;
            errorMessage = e.getMessage();
            log.error("Exception while processing transaction event", e);
        } finally {
            monitoringEventPublisher.publish(monitoringEventPublisher.metric(
                    trace,
                    MonitoringEventPublisher.KAFKA_CONSUMER,
                    "CONSUME",
                    null,
                    eventTransactionDto.getDestination(),
                    System.currentTimeMillis() - start,
                    error ? 500 : 200,
                    error,
                    errorMessage
            ));
            TraceContext.clear();
        }
    }
}
