package org.ricramiel.creditservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.*;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.creditservice.infrastructure.CreditServiceImpl;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.model.CreditRating;
import org.ricramiel.creditservice.model.CreditTemp;
import org.ricramiel.creditservice.model.PaymentHistoryRecord;
import org.ricramiel.creditservice.repository.CreditRatingRepository;
import org.ricramiel.creditservice.repository.CreditRepository;
import org.ricramiel.creditservice.repository.CreditTempRepository;
import org.ricramiel.creditservice.repository.PaymentHistoryRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final PaymentHistoryRecordRepository paymentHistoryRecordRepository;

    @Value("${app.kafka.destination}")
    private String type;

    private final CreditServiceImpl creditService;
    private final CreditTempRepository creditTempRepository;
    private final CreditRepository creditRepository;
    private final CreditRatingRepository creditRatingRepository;

    @Transactional
    @KafkaListener(topics = {"${app.kafka.topics.withdraw}", "TransactionEnroll_credit"}, groupId = "withdraw")
    public void listenWithAck(@Payload EventTransactionDto eventTransactionDto, Acknowledgment acknowledgment) {
        try {

            TransactionKafkaDto transactionKafkaDto = eventTransactionDto.getData();

            if (!Objects.equals(eventTransactionDto.getDestination(), type)) {
                log.error("Received unexpected withdraw event with destination {}", eventTransactionDto.getDestination());
                acknowledgment.acknowledge();
                return;
            }

            if (transactionKafkaDto.getAction().equals("Погашение кредита")) {

                PaymentHistoryRecord paymentHistoryRecord = paymentHistoryRecordRepository.findById(transactionKafkaDto.getSourceId()).orElseThrow();
                paymentHistoryRecord.setTransactionStatus(transactionKafkaDto.getTransactionStatus());
                paymentHistoryRecordRepository.save(paymentHistoryRecord);

            }

            if (transactionKafkaDto.getTransactionStatus().equals(TransactionStatus.COMPLETE)
                    && transactionKafkaDto.getAction().equals("Погашение кредита")) {
                creditService.makeEnrollment(transactionKafkaDto.getAccountId(), transactionKafkaDto.getMoney());
            }


            if (transactionKafkaDto.getTransactionStatus().equals(TransactionStatus.COMPLETE)
                    && transactionKafkaDto.getAction().equals("Создание кредита")) {

                CreditTemp creditTemp = creditTempRepository.findById(transactionKafkaDto.getSourceId()).orElseThrow();

                Credit credit = Credit.builder()
                        .creditRule(creditTemp.getCreditRule())
                        .initialDebt(creditTemp.getInitialDebt())
                        .interestDebtSum(creditTemp.getInterestDebtSum())
                        .lastInterestUpdate(LocalDateTime.now())
                        .currency(creditTemp.getCurrency())
                        .cardAccount(creditTemp.getCardAccount())
                        .currentDebtSum(creditTemp.getInitialDebt())
                        .userId(creditTemp.getUserId())
                        .build();

                credit = creditRepository.save(credit);

                CreditRating creditRating = new CreditRating();
                creditRating.setRating(BigDecimal.valueOf(100));
                creditRating.setUserId(credit.getUserId());
                creditRatingRepository.save(creditRating);

                creditTempRepository.deleteById(creditTemp.getId());
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Exception while processing withdraw event", e);
        }
    }
}
