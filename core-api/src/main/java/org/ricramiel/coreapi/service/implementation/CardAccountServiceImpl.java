package org.ricramiel.coreapi.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.entity.OutboxEvent;
import org.ricramiel.coreapi.entity.TransactionOperation;
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


    //refactor
    @Override
    @Transactional
    public void enroll(EnrollDto enrollDto) {
        CardAccount account = cardAccountRepository.findById(enrollDto.getCardAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.setMoney(account.getMoney().add(enrollDto.getSum()));
        cardAccountRepository.save(account);
        TransactionOperation transactionOperation = new TransactionOperation();
        transactionOperation.setAccount(account);
        transactionOperation.setMoney(enrollDto.getSum());
        transactionOperation.setTransactionType(TransactionType.ENROLLMENT);
        transactionOperation.setTransactionStatus(TransactionStatus.COMPLETE);
        transactionOperation.setAction(enrollDto.getDestination());
        transactionOperation.setDateTime(LocalDateTime.now());
        transactionOperationRepository.save(transactionOperation);
    }

    //refactor
    @Override
    @Transactional
    @SneakyThrows
    public void withdraw(WithdrawDto withdrawDto) {
        CardAccount account = cardAccountRepository.findById(withdrawDto.getCardAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));
        if(account.getMoney().compareTo(withdrawDto.getSum()) < 0) {
            log.debug("Withdraw transaction has been denied, due to not enough money");
            // TODO Обработка недостатка средств с TransactionStatus = Declined
        }
        account.setMoney(account.getMoney().subtract(withdrawDto.getSum()));
        cardAccountRepository.save(account);

        TransactionOperation transactionOperation = new TransactionOperation();
        transactionOperation.setAccount(account);
        transactionOperation.setMoney(withdrawDto.getSum());
        transactionOperation.setTransactionType(TransactionType.WITHDRAWAL);
        transactionOperation.setTransactionStatus(TransactionStatus.COMPLETE);
        transactionOperation.setAction(withdrawDto.getDestination());
        transactionOperation.setDateTime(LocalDateTime.now());
        transactionOperationRepository.save(transactionOperation);
    }

    @Override
    public Boolean checkAccountExistance(UUID accountId) {
        return cardAccountRepository.existsById(accountId);
    }

    @Override
    public CardAccount createAccount(UUID userId) {
        CardAccount cardAccount = CardAccount.builder()
                .userId(userId)
                .money(BigDecimal.ZERO)
                .deleted(false)
                .build();
        return cardAccountRepository.save(cardAccount);
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
}
