package org.ricramiel.coreapi.repository;

import org.ricramiel.coreapi.entity.TransactionOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


import java.util.UUID;

@Repository
public interface TransactionOperationRepository extends JpaRepository<TransactionOperation, UUID>, PagingAndSortingRepository<TransactionOperation, UUID> {
    Page<TransactionOperation> findAllByAccountId(UUID accountId, Pageable pageable);
}
