package org.ricramiel.creditservice.infrastructure;

import lombok.RequiredArgsConstructor;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.model.PaymentHistoryRecord;
import org.ricramiel.creditservice.repository.PaymentHistoryRecordRepository;
import org.ricramiel.creditservice.service.CreditService;
import org.ricramiel.creditservice.service.PaymentHistoryRecordService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentHistoryRecordServiceImpl implements PaymentHistoryRecordService {

    private final PaymentHistoryRecordRepository paymentHistoryRecordRepository;
    private final CreditService creditService;

    @Override
    public PaymentHistoryRecord createHistoryRecord(PaymentHistoryRecord paymentHistoryRecord) {
        return paymentHistoryRecordRepository.save(paymentHistoryRecord);
    }

    @Override
    public List<PaymentHistoryRecord> getHistoryByUserId(UUID userId) {

        List<Credit> credits = creditService.getByUserId(userId);
        List<UUID> cardAccounts = new ArrayList<>();
        List<PaymentHistoryRecord> paymentHistoryRecords = new ArrayList<>();

        for (Credit credit : credits) {
            cardAccounts.add(credit.getCardAccount());
        }

        for (UUID cardAccount : cardAccounts) {
            paymentHistoryRecords.addAll(getHistoryByCardAccount(cardAccount));
        }

        return paymentHistoryRecords;
    }

    @Override
    public List<PaymentHistoryRecord> getHistoryByCardAccount(UUID cardAccount) {
        return paymentHistoryRecordRepository.findAllByCardAccount(cardAccount);
    }
}
