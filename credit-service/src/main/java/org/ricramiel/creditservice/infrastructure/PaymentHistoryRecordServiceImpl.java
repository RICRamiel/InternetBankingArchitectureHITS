package org.ricramiel.creditservice.infrastructure;

import lombok.RequiredArgsConstructor;
import org.ricramiel.creditservice.model.PaymentHistoryRecord;
import org.ricramiel.creditservice.repository.PaymentHistoryRecordRepository;
import org.ricramiel.creditservice.service.PaymentHistoryRecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentHistoryRecordServiceImpl implements PaymentHistoryRecordService {

    private final PaymentHistoryRecordRepository paymentHistoryRecordRepository;

    @Override
    public PaymentHistoryRecord createHistoryRecord(PaymentHistoryRecord paymentHistoryRecord) {
        return paymentHistoryRecordRepository.save(paymentHistoryRecord);
    }

    @Override
    public List<PaymentHistoryRecord> getHistoryByUserId(UUID userId) {
        return paymentHistoryRecordRepository.findAllByUserId(userId);
    }

    @Override
    public List<PaymentHistoryRecord> getHistoryByCardAccount(UUID cardAccount) {
        return paymentHistoryRecordRepository.findAllByCardAccount(cardAccount);
    }
}
