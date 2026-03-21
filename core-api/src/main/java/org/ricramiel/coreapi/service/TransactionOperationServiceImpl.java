package org.ricramiel.coreapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.ricramiel.coreapi.repository.TransactionOperationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionOperationServiceImpl {

    private final TransactionOperationRepository transactionOperationRepository;
    private final WebSocketPushService webSocketPushService;

    @Transactional(readOnly = true)
    public Page<TransactionOperation> findByAccountId(UUID accountId, Pageable pageable) {
        return transactionOperationRepository.findAllByAccountId(accountId, pageable);
    }
}