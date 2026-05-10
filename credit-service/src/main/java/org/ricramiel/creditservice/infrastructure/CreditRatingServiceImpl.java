package org.ricramiel.creditservice.infrastructure;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.creditservice.model.CreditRating;
import org.ricramiel.creditservice.model.PaymentHistoryRecord;
import org.ricramiel.creditservice.repository.CreditRatingRepository;
import org.ricramiel.creditservice.service.CreditRatingService;
import org.ricramiel.creditservice.service.PaymentHistoryRecordService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditRatingServiceImpl implements CreditRatingService {

    private static final BigDecimal DEFAULT_RATING = BigDecimal.valueOf(100);

    private final CreditRatingRepository creditRatingRepository;
    private final PaymentHistoryRecordService paymentHistoryRecordService;

    @Override
    public CreditRating getByUserId(UUID userId) {
        CreditRating creditRating = creditRatingRepository.findByUserId(userId);
        if (creditRating == null) {
            creditRating = new CreditRating();
            creditRating.setUserId(userId);
        }

        creditRating.setRating(creditRatingCount(userId));
        return creditRatingRepository.save(creditRating);
    }

    private BigDecimal creditRatingCount(UUID userId) {

        BigDecimal success = BigDecimal.ZERO;
        BigDecimal fault = BigDecimal.ZERO;

        List<PaymentHistoryRecord> paymentHistoryRecords = paymentHistoryRecordService.getHistoryByUserId(userId);

        if (paymentHistoryRecords.isEmpty()) {
            return DEFAULT_RATING;
        }

        for (PaymentHistoryRecord paymentHistoryRecord : paymentHistoryRecords) {
            if (paymentHistoryRecord.getTransactionStatus() == TransactionStatus.COMPLETE) {
                success = success.add(BigDecimal.ONE);
            } else if (paymentHistoryRecord.getTransactionStatus() == TransactionStatus.DECLINED) {
                fault = fault.add(BigDecimal.ONE);
            }
        }

        BigDecimal total = success.add(fault);
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return DEFAULT_RATING;
        }

        BigDecimal rating = success.divide(total, 2, RoundingMode.HALF_UP);
        rating = rating.multiply(BigDecimal.valueOf(100));

        return rating;
    }
}
