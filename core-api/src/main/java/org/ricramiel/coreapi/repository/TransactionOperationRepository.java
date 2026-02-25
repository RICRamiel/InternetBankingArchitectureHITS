package org.ricramiel.coreapi.repository;

import org.ricramiel.coreapi.entity.TransactionOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionOperationRepository extends JpaRepository<TransactionOperation, Long> {
    Page<TransactionOperation> findAllByAccountId(Long accountId, Pageable pageable);
}
