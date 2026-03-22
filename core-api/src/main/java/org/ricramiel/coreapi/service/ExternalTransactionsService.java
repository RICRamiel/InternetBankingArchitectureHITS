package org.ricramiel.coreapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.exceptions.status_code_exceptions.BadRequestException;
import org.ricramiel.coreapi.entity.OutboxEvent;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.ricramiel.coreapi.model.EnrollRequest;
import org.ricramiel.coreapi.model.TransferCurrencyRequest;
import org.ricramiel.coreapi.model.TransferRequest;
import org.ricramiel.coreapi.model.TransferResult;
import org.ricramiel.coreapi.model.WithdrawRequest;
import org.ricramiel.coreapi.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalTransactionsService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final InternalTransactionsService internalTransactionsService;

    @Value("${type.withdraw}")
    private String TYPE_WITHDRAW;

    @Value("${spring.kafka.topic.withdraw_transaction}")
    private String WITHDRAW_TRANSACTION_TOPIC;

    @Value("${spring.kafka.topic.enroll_transaction}")
    private String ENROLL_TRANSACTION_TOPIC;


    @SneakyThrows
    @Transactional
    public void enroll(TransactionKafkaDto dto, boolean fromMasterAccount, String desti) {
        TransactionOperation saved;
        if (fromMasterAccount) {
            saved = internalTransactionsService.enrollFromMasterAccount(EnrollRequest.builder()
                    .cardAccountId(dto.getAccountId())
                    .sum(dto.getMoney())
                    .currency(dto.getCurrency())
                    .build(), dto.getAction()).getEnrollmentOperation();
        }
        else{
            saved = internalTransactionsService.enroll(EnrollRequest.builder()
                    .cardAccountId(dto.getAccountId())
                    .sum(dto.getMoney())
                    .currency(dto.getCurrency())
                    .build(), dto.getAction());
        }

        //form kafkaEvent
        dto.setId(saved.getId());
        dto.setTransactionStatus(saved.getTransactionStatus());
        String dest = (!StringUtils.isEmpty(desti)) ? desti : "client";
        EventTransactionDto kafkaDto = new EventTransactionDto(UUID.randomUUID(), dto, LocalDateTime.now(), dest);
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(ENROLL_TRANSACTION_TOPIC + "_" + dest);
        outboxEvent.setPayload(objectMapper.writeValueAsString(kafkaDto));
        outboxRepository.save(outboxEvent);
    }

    @Transactional
    @SneakyThrows
    public void withdraw(TransactionKafkaDto dto, boolean toMasterAccount, String desti) {
        TransactionOperation saved;
        if (toMasterAccount) {
            saved = internalTransactionsService.withdrawToMasterAccount(WithdrawRequest.builder()
                    .cardAccountId(dto.getAccountId())
                    .sum(dto.getMoney())
                    .currency(dto.getCurrency())
                    .build(), dto.getAction()).getWithdrawalOperation();
        }
        else{
            saved = internalTransactionsService.withdraw(WithdrawRequest.builder()
                    .cardAccountId(dto.getAccountId())
                    .sum(dto.getMoney())
                    .currency(dto.getCurrency())
                    .build(), dto.getAction());
        }

        //form kafkaEvent
        dto.setId(saved.getId());
        dto.setTransactionStatus(saved.getTransactionStatus());
        EventTransactionDto kafkaDto = new EventTransactionDto(UUID.randomUUID(), dto, LocalDateTime.now(), TYPE_WITHDRAW);
        String dest = (!StringUtils.isEmpty(desti)) ? desti : "client";
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(WITHDRAW_TRANSACTION_TOPIC + "_" + dest);
        outboxEvent.setPayload(objectMapper.writeValueAsString(kafkaDto));
        outboxRepository.save(outboxEvent);
    }

    @Transactional
    public void transfer(TransferRequest model) {
        TransferResult result = internalTransactionsService.transfer(model);
        if (!result.isSucceded()){
            throw new BadRequestException("Transfer failed");
        }
    }

    @Transactional
    public void transfer(TransferCurrencyRequest model) {
        TransferResult result = internalTransactionsService.transfer(model);
        if (!result.isSucceded()){
            throw new BadRequestException("Transfer failed");
        }
    }
}