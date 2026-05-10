package org.ricramiel.coreapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventAccountCreate;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.common.tracing.TraceHeaders;
import org.ricramiel.coreapi.dto.CardAccountCreateDto;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.entity.OutboxAccountEvent;
import org.ricramiel.coreapi.exception.CardAccountNameAlreadyUsedException;
import org.ricramiel.coreapi.repository.CardAccountRepository;
import org.ricramiel.coreapi.repository.OutboxAccountEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardAccountServiceImpl {
    private final CardAccountRepository cardAccountRepository;
    private final InternalTransactionsService internalTransactionsService;
    private final OutboxAccountEventRepository outboxAccountEventRepository;
    private final ObjectMapper objectMapper;

    public Boolean checkAccountExistance(UUID accountId) {
        return cardAccountRepository.existsById(accountId);
    }

    @Transactional
    @SneakyThrows
    public CardAccount createAccount(UUID userId, CardAccountCreateDto dto) {
        //Реализовать уникальный нейминг в рамках пользователя
        if (checkUnicNameByUser(userId, dto.getName())) {
            CardAccount cardAccount = CardAccount.builder()
                    .userId(userId)
                    .money(BigDecimal.ZERO)
                    .deleted(false)
                    .name(dto.getName())
                    .currency(dto.getCurrency().toUpperCase())
                    .isMain((dto.getIsMain() != null) && dto.getIsMain())
                    .build();
            CardAccount savedCardAccount = cardAccountRepository.save(cardAccount);
            EventAccountCreate event = new EventAccountCreate(savedCardAccount.getId(), savedCardAccount.getUserId());
            TraceHeaders.applyCurrentTrace(event);
            outboxAccountEventRepository.save(OutboxAccountEvent.builder()
                    .payload(objectMapper.writeValueAsString(event))
                    .outboxTopic("account-event")
                    .build());
            return savedCardAccount;
        } else {
            throw new CardAccountNameAlreadyUsedException("Account with this name already exists");
        }
    }

    public Boolean closeAccount(UUID accountId) {
        CardAccount account = cardAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.setDeleted(true);
        cardAccountRepository.save(account);
        return true;
    }

    public Page<CardAccount> getUserCardAccounts(UUID userId, Pageable pageable) {
        return cardAccountRepository.findByUserId(userId, pageable);
    }

    public CardAccount getAccountById(UUID accountId) {
        return cardAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    /**
     * Функция чека на уникальность имени счёта среди других счетов пользователя
     */
    private boolean checkUnicNameByUser(UUID userId, String name) {
        return cardAccountRepository.countByUserIdAndName(userId, name) <= 0;
    }

    public CardAccount setMainAccount(UUID accountId) {
        CardAccount account = cardAccountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("Account not found"));
        account.setIsMain((account.getIsMain() != null) && account.getIsMain());
        return account;
    }
}
