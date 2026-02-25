package org.ricramiel.coreapi.service;

import org.ricramiel.coreapi.entity.TransactionOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface TransactionOperationService {
    Page<TransactionOperation> findByAccountId(UUID accountId, Pageable pageable);
}
