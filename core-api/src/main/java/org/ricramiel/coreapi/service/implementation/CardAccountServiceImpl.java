package org.ricramiel.coreapi.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.ricramiel.common.dtos.EventWithdrawDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.entity.OutboxEvent;
import org.ricramiel.coreapi.repository.CardAccountRepository;
import org.ricramiel.coreapi.repository.OutboxRepository;
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
public class CardAccountServiceImpl implements CardAccountService {
    private final CardAccountRepository cardAccountRepository;
    private final OutboxRepository outboxRepository;
    private ObjectMapper objectMapper;

    @Value("type.withdraw")
    private String TYPE_WITHDRAW;

    @Value("spring.kafka.topic.withdraw_transaction")
    private String WITHDRAW_TRANSACTION_TOPIC;

    @Override
    @Transactional
    public void enroll(UUID accountId, BigDecimal amount) {
        CardAccount account = cardAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.setMoney(account.getMoney().add(amount));
    }

    @Override
    @Transactional
    @SneakyThrows
    public void withdraw(WithdrawDto withdrawDto) {
        CardAccount account = cardAccountRepository.findById(withdrawDto.getCardAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.setMoney(account.getMoney().subtract(withdrawDto.getSum()));
        EventWithdrawDto eventWithdrawDto = new EventWithdrawDto(UUID.randomUUID(), withdrawDto, LocalDateTime.now(), TYPE_WITHDRAW);
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(WITHDRAW_TRANSACTION_TOPIC);
        outboxEvent.setPayload(objectMapper.writeValueAsString(eventWithdrawDto));
        outboxRepository.save(outboxEvent);
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
