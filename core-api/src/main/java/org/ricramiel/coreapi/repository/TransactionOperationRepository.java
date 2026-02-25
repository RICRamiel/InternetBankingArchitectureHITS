package org.ricramiel.coreapi.repository;

import org.ricramiel.coreapi.entity.TransactionOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionOperationRepository extends JpaRepository<TransactionOperation, Long> {
}
