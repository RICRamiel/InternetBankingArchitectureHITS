package org.ricramiel.coreapi.service;

import org.ricramiel.coreapi.entity.TransactionOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface TransactionOperationService {
    Page<TransactionOperation> findByAccountId(Long accountId, Pageable pageable);
}
