package org.ricramiel.transactionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.transactionservice.entity.OperationHistory;
import org.ricramiel.transactionservice.entity.OutboxEvent;
import org.ricramiel.transactionservice.mapper.TransactionMapper;
import org.ricramiel.transactionservice.repository.OperationHistoryRepository;
import org.ricramiel.transactionservice.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransactionService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final OperationHistoryRepository operationHistoryRepository;

    @Value("${type.withdraw}")
    private String TYPE_WITHDRAW;

    @Value("${type.enroll}")
    private String TYPE_ENROLL;

    @Value("${spring.kafka.topic.withdraw_transaction}")
    private String WITHDRAW_TRANSACTION_TOPIC;

    @Value("${spring.kafka.topic.enroll_transaction}")
    private String ENROLL_TRANSACTION_TOPIC;


    @SneakyThrows
    public void withdraw(WithdrawDto withdrawDto) {
        log.info("withdraw: {}", withdrawDto);
        String dest = (!withdrawDto.getDestination().isEmpty()) ? withdrawDto.getDestination() : "client";
        TransactionKafkaDto transactionKafkaDto = TransactionMapper.toTransactionKafkaDto(withdrawDto, dest);

        OperationHistory operationHistory = TransactionMapper.fromTransactionKafkaDto(transactionKafkaDto);
        operationHistoryRepository.save(operationHistory);

        EventTransactionDto kafkaDto = new EventTransactionDto(UUID.randomUUID(), transactionKafkaDto, LocalDateTime.now(), TYPE_WITHDRAW);
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(WITHDRAW_TRANSACTION_TOPIC + "_" + dest);
        outboxEvent.setPayload(objectMapper.writeValueAsString(kafkaDto));
        outboxRepository.save(outboxEvent);
    }

    @SneakyThrows
    public void enroll(EnrollDto enrollDto) {
        log.info("enroll: {}", enrollDto);
        String dest = (!enrollDto.getDestination().isEmpty()) ? enrollDto.getDestination() : "client";
        TransactionKafkaDto transactionKafkaDto = TransactionMapper.toTransactionKafkaDto(enrollDto, dest);

        OperationHistory operationHistory = TransactionMapper.fromTransactionKafkaDto(transactionKafkaDto);
        operationHistoryRepository.save(operationHistory);

        EventTransactionDto kafkaDto = new EventTransactionDto(UUID.randomUUID(), transactionKafkaDto, LocalDateTime.now(), TYPE_ENROLL);
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(ENROLL_TRANSACTION_TOPIC + "_" + dest);
        outboxEvent.setPayload(objectMapper.writeValueAsString(kafkaDto));
        outboxRepository.save(outboxEvent);
    }
}
