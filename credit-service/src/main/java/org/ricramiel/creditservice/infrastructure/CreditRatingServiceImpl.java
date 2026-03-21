package org.ricramiel.creditservice.infrastructure;

import lombok.RequiredArgsConstructor;
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

    private final CreditRatingRepository creditRatingRepository;
    private final PaymentHistoryRecordService paymentHistoryRecordService;

    @Override
    public CreditRating getByUserId(UUID userId) {
        CreditRating creditRating = creditRatingRepository.findByUserId(userId);
        creditRating.setRating(creditRatingCount(creditRating));
        return creditRating;
    }

    private BigDecimal creditRatingCount(CreditRating creditRating) {

        BigDecimal success = BigDecimal.ZERO;
        BigDecimal fault = BigDecimal.ZERO;

        List<PaymentHistoryRecord> paymentHistoryRecords = paymentHistoryRecordService.getHistoryByUserId(creditRating.getUserId());

        for (int i = 0; i < (long) paymentHistoryRecords.size(); i++) {
            success = success.add(BigDecimal.ONE);
            fault = fault.add(BigDecimal.ONE);
        }

        BigDecimal rating = fault.divide(success.add(fault), RoundingMode.CEILING);
        rating = rating.multiply(BigDecimal.valueOf(100));

        return rating;
    }
}
