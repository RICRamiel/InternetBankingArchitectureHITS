package org.ricramiel.coreapi.service.implementation;

import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.ricramiel.coreapi.repository.TransactionOperationRepository;
import org.ricramiel.coreapi.service.TransactionOperationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionOperationServiceImpl implements TransactionOperationService {
    private final TransactionOperationRepository transactionOperationRepository;

    @Override
    public Page<TransactionOperation> findByAccountId(UUID accountId, Pageable pageable) {
        return transactionOperationRepository.findAllByAccountId(accountId, pageable);
    }
}
