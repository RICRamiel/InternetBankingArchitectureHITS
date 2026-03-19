package org.ricramiel.creditservice.service;

import org.ricramiel.creditservice.model.PaymentHistoryRecord;

import java.util.List;
import java.util.UUID;

public interface PaymentHistoryRepositoryService {
    PaymentHistoryRecord createHistoryRecord(PaymentHistoryRecord paymentHistoryRecord);
    List<PaymentHistoryRecord> getHistoryByUserId(UUID userId);
    List<PaymentHistoryRecord> getHistoryByCardAccount(UUID cardAccount);
}
