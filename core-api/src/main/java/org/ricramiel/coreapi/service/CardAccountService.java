package org.ricramiel.coreapi.service;

import org.ricramiel.coreapi.entity.CardAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface CardAccountService {
    void enroll(Long accountId, BigDecimal amount);
    void withdraw(Long accountId, BigDecimal amount);
    Boolean checkAccountExistance(Long accountId);
    CardAccount createAccount(Long userId);
    Boolean closeAccount(Long accountId);
    Page<CardAccount> getUserCardAccounts(Long userId, Pageable pageable);
    CardAccount getAccountById(Long accountId);
}
