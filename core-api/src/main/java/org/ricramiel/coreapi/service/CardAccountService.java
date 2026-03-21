package org.ricramiel.coreapi.service;

import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.dtos.TransferDto;
import org.ricramiel.coreapi.dto.CardAccountCreateDto;
import org.ricramiel.coreapi.entity.CardAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface CardAccountService {
    void enroll(TransactionKafkaDto dto, String topicDest);
    void withdraw(TransactionKafkaDto dto, String topicDest);
    Boolean checkAccountExistance(UUID accountId);
    CardAccount createAccount(UUID userId, CardAccountCreateDto dto);
    Boolean closeAccount(UUID accountId);
    Page<CardAccount> getUserCardAccounts(UUID userId, Pageable pageable);
    CardAccount getAccountById(UUID accountId);

    void transfer(TransferDto enrollDto);
}
