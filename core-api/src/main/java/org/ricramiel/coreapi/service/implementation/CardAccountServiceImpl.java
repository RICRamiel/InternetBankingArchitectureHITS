package org.ricramiel.coreapi.service.implementation;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.repository.CardAccountRepository;
import org.ricramiel.coreapi.service.CardAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardAccountServiceImpl implements CardAccountService {
    private final CardAccountRepository cardAccountRepository;

    @Override
    public void enroll(UUID accountId, BigDecimal amount) {
    }

    @Override
    public void withdraw(UUID accountId, BigDecimal amount) {

    }

    @Override
    public Boolean checkAccountExistance(UUID accountId) {
        return null;
    }

    @Override
    public CardAccount createAccount(UUID userId) {
        return null;
    }

    @Override
    public Boolean closeAccount(UUID accountId) {
        return null;
    }

    @Override
    public Page<CardAccount> getUserCardAccounts(UUID userId, Pageable pageable) {
        return null;
    }

    @Override
    public CardAccount getAccountById(UUID accountId) {
        return null;
    }
}
