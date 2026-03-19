package org.ricramiel.transactionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.EventEnrollDto;
import org.ricramiel.common.dtos.EventWithdrawDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.transactionservice.entity.OutboxEvent;
import org.ricramiel.transactionservice.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

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
        EventWithdrawDto eventWithdrawDto =
                new EventWithdrawDto(UUID.randomUUID(), withdrawDto, LocalDateTime.now(), TYPE_WITHDRAW);
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(WITHDRAW_TRANSACTION_TOPIC + "_" + dest);
        outboxEvent.setPayload(objectMapper.writeValueAsString(eventWithdrawDto));
        outboxRepository.save(outboxEvent);
    }

    @SneakyThrows
    public void enroll(EnrollDto enrollDto) {
        log.info("enroll: {}", enrollDto);
        String dest = (!enrollDto.getDestination().isEmpty()) ? enrollDto.getDestination() : "client";
        EventEnrollDto eventEnrollDto =
                new EventEnrollDto(UUID.randomUUID(), enrollDto, LocalDateTime.now(), TYPE_ENROLL);
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(ENROLL_TRANSACTION_TOPIC + "_" + dest);
        outboxEvent.setPayload(objectMapper.writeValueAsString(eventEnrollDto));
        outboxRepository.save(outboxEvent);
    }
}
