package org.ricramiel.coreapi.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.coreapi.dto.CardAccountCreateDto;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.entity.OutboxEvent;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.ricramiel.coreapi.exception.CardAccountNameAlreadyUsedException;
import org.ricramiel.coreapi.repository.CardAccountRepository;
import org.ricramiel.coreapi.repository.OutboxRepository;
import org.ricramiel.coreapi.repository.TransactionOperationRepository;
import org.ricramiel.coreapi.service.CardAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardAccountServiceImpl implements CardAccountService {
    private final CardAccountRepository cardAccountRepository;
    private final OutboxRepository outboxRepository;
    private final TransactionOperationRepository transactionOperationRepository;
    private final ObjectMapper objectMapper;

    @Value("${type.withdraw}")
    private String TYPE_WITHDRAW;

    @Value("${spring.kafka.topic.withdraw_transaction}")
    private String WITHDRAW_TRANSACTION_TOPIC;

    @Value("${spring.kafka.topic.enroll_transaction}")
    private String ENROLL_TRANSACTION_TOPIC;

    //refactor
    @Override
    @SneakyThrows
    @Transactional
    public void enroll(TransactionKafkaDto dto) {
        CardAccount account = cardAccountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.setMoney(account.getMoney().add(dto.getMoney()));
        cardAccountRepository.save(account);
        TransactionOperation transactionOperation = new TransactionOperation();
        transactionOperation.setAccount(account);
        transactionOperation.setMoney(dto.getMoney());
        transactionOperation.setTransactionType(dto.getTransactionType());
        transactionOperation.setTransactionStatus(TransactionStatus.COMPLETE);
        transactionOperation.setAction(dto.getAction());
        transactionOperation.setDateTime(dto.getDateTime());
        TransactionOperation saved = transactionOperationRepository.save(transactionOperation);

        //form kafkaEvent
        dto.setId(saved.getId());
        dto.setTransactionStatus(saved.getTransactionStatus());
        EventTransactionDto kafkaDto = new EventTransactionDto(UUID.randomUUID(), dto, LocalDateTime.now(), TYPE_WITHDRAW);
        String dest = (!saved.getAction().isEmpty()) ? dto.getAction() : "client";
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(ENROLL_TRANSACTION_TOPIC + "_" + dest);
        outboxEvent.setPayload(objectMapper.writeValueAsString(kafkaDto));
        outboxRepository.save(outboxEvent);
    }

    //refactor
    @Override
    @Transactional
    @SneakyThrows
    public void withdraw(TransactionKafkaDto dto) {
        TransactionOperation transactionOperation = new TransactionOperation();
        CardAccount account = cardAccountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (account.getMoney().compareTo(dto.getMoney()) >= 0) {
            log.debug("Withdraw transaction has been denied, due to not enough money");
            account.setMoney(account.getMoney().subtract(dto.getMoney()));
            cardAccountRepository.save(account);
            transactionOperation.setTransactionStatus(TransactionStatus.DECLINED);
        } else {
            transactionOperation.setTransactionStatus(TransactionStatus.COMPLETE);
        }
        transactionOperation.setAccount(account);
        transactionOperation.setMoney(dto.getMoney());
        transactionOperation.setTransactionType(dto.getTransactionType());
        transactionOperation.setAction(dto.getAction());
        transactionOperation.setDateTime(dto.getDateTime());
        TransactionOperation saved = transactionOperationRepository.save(transactionOperation);

        //form kafkaEvent
        dto.setId(saved.getId());
        dto.setTransactionStatus(saved.getTransactionStatus());
        EventTransactionDto kafkaDto = new EventTransactionDto(UUID.randomUUID(), dto, LocalDateTime.now(), TYPE_WITHDRAW);
        String dest = (!saved.getAction().isEmpty()) ? dto.getAction() : "client";
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(WITHDRAW_TRANSACTION_TOPIC + "_" + dest);
        outboxEvent.setPayload(objectMapper.writeValueAsString(kafkaDto));
        outboxRepository.save(outboxEvent);
    }

    @Override
    public Boolean checkAccountExistance(UUID accountId) {
        return cardAccountRepository.existsById(accountId);
    }

    @Override
    public CardAccount createAccount(UUID userId, CardAccountCreateDto dto) {
        //Реализовать уникальный нейминг в рамках пользователя
        if (checkUnicNameByUser(userId, dto.getName())) {
            CardAccount cardAccount = CardAccount.builder()
                    .userId(userId)
                    .money(BigDecimal.ZERO)
                    .deleted(false)
                    .name(dto.getName())
                    .currency(dto.getCurrency())
                    .isMain((dto.getIsMain() != null) && dto.getIsMain())
                    .build();
            return cardAccountRepository.save(cardAccount);
        } else {
            throw new CardAccountNameAlreadyUsedException("Account with this name already exists");
        }
    }

    @Override
    public Boolean closeAccount(UUID accountId) {
        CardAccount account = cardAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.setDeleted(true);
        cardAccountRepository.save(account);
        return true;
    }

    @Override
    public Page<CardAccount> getUserCardAccounts(UUID userId, Pageable pageable) {
        return cardAccountRepository.findByUserId(userId, pageable);
    }

    @Override
    public CardAccount getAccountById(UUID accountId) {
        return cardAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    /**
     * Функция чека на уникальность имени счёта среди других счетов пользователя
     */
    private Boolean checkUnicNameByUser(UUID userId, String name) {
        return cardAccountRepository.countByUserIdAndName(userId, name) <= 0;
    }
}
