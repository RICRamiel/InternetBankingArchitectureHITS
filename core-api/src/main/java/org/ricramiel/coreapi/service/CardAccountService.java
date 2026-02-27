package org.ricramiel.coreapi.service;

import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.coreapi.entity.CardAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public interface CardAccountService {
    void enroll(UUID accountId, BigDecimal amount);
    void withdraw(WithdrawDto withdrawDto);
    Boolean checkAccountExistance(UUID accountId);
    CardAccount createAccount(UUID userId);
    Boolean closeAccount(UUID accountId);
    Page<CardAccount> getUserCardAccounts(UUID userId, Pageable pageable);
    CardAccount getAccountById(UUID accountId);
}
