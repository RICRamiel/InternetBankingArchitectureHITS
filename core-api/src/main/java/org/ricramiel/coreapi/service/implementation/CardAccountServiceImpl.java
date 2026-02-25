package org.ricramiel.coreapi.service.implementation;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.repository.CardAccountRepository;
import org.ricramiel.coreapi.service.CardAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardAccountServiceImpl implements CardAccountService {
    private final CardAccountRepository cardAccountRepository;

    @Override
    public void enroll(Long accountId, BigDecimal amount) {
    }

    @Override
    public void withdraw(Long accountId, BigDecimal amount) {

    }

    @Override
    public Boolean checkAccountExistance(Long accountId) {
        return null;
    }

    @Override
    public CardAccount createAccount(Long userId) {
        return null;
    }

    @Override
    public Boolean closeAccount(Long accountId) {
        return null;
    }

    @Override
    public Page<CardAccount> getUserCardAccounts(Long userId, Pageable pageable) {
        return null;
    }

    @Override
    public CardAccount getAccountById(Long accountId) {
        return null;
    }
}
