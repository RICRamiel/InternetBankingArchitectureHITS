package org.ricramiel.creditservice.repository;

import org.ricramiel.creditservice.model.PaymentHistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentHistoryRecordRepository extends JpaRepository<PaymentHistoryRecord, UUID> {

    List<PaymentHistoryRecord> findAllByCardAccount(UUID cardAccount);
}
