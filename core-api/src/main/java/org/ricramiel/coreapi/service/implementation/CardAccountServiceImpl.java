package org.ricramiel.coreapi.service.implementation;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.repository.CardAccountRepository;
import org.ricramiel.coreapi.service.CardAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.smartcardio.Card;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardAccountServiceImpl implements CardAccountService {
    private final CardAccountRepository cardAccountRepository;

    @Override
    public void enroll(UUID accountId, BigDecimal amount) {
        CardAccount account = cardAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.setMoney(account.getMoney().add(amount));
    }

    @Override
    public void withdraw(UUID accountId, BigDecimal amount) {
        CardAccount account = cardAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.setMoney(account.getMoney().subtract(amount));
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
