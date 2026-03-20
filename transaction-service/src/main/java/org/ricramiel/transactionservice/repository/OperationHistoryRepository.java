package org.ricramiel.transactionservice.repository;

import org.ricramiel.transactionservice.entity.OperationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OperationHistoryRepository extends JpaRepository<OperationHistory, UUID> {
}
