package org.ricramiel.creditservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;
import org.ricramiel.common.tracing.MonitoringEventPublisher;
import org.ricramiel.common.tracing.TraceContext;
import org.ricramiel.common.tracing.TraceHeaders;
import org.ricramiel.creditservice.infrastructure.CreditServiceImpl;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.model.CreditRating;
import org.ricramiel.creditservice.model.CreditTemp;
import org.ricramiel.creditservice.model.IdempotencyKey;
import org.ricramiel.creditservice.model.PaymentHistoryRecord;
import org.ricramiel.creditservice.repository.CreditRatingRepository;
import org.ricramiel.creditservice.repository.CreditRepository;
import org.ricramiel.creditservice.repository.CreditTempRepository;
import org.ricramiel.creditservice.repository.IdempotencyKeyRepository;
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
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final MonitoringEventPublisher monitoringEventPublisher;

    @Transactional
    @KafkaListener(topics = {"${app.kafka.topics.withdraw}", "TransactionEnroll_credit"}, groupId = "withdraw")
    public void listenWithAck(@Payload EventTransactionDto eventTransactionDto, Acknowledgment acknowledgment) {
        long start = System.currentTimeMillis();
        TraceContext.TraceState trace = TraceHeaders.openFrom(eventTransactionDto);
        boolean error = false;
        String errorMessage = null;

        try {
            if (!idempotencyKeyRepository.existsById(eventTransactionDto.getId())) {
                TransactionKafkaDto transactionKafkaDto = eventTransactionDto.getData();

                if (!Objects.equals(eventTransactionDto.getDestination(), type)) {
                    log.error("Received unexpected withdraw event with destination {}", eventTransactionDto.getDestination());
                    acknowledgment.acknowledge();
                    return;
                }

                if (isCompletedCreditCreation(transactionKafkaDto)) {
                    createCreditFromTemp(transactionKafkaDto);

                    IdempotencyKey idempotencyKey = IdempotencyKey.builder().id(eventTransactionDto.getId()).build();
                    idempotencyKeyRepository.save(idempotencyKey);

                    acknowledgment.acknowledge();
                    return;
                }

                if (isCreditPayback(transactionKafkaDto)) {
                    PaymentHistoryRecord paymentHistoryRecord = paymentHistoryRecordRepository.findById(transactionKafkaDto.getSourceId()).orElseThrow();
                    paymentHistoryRecord.setTransactionStatus(transactionKafkaDto.getTransactionStatus());
                    paymentHistoryRecordRepository.save(paymentHistoryRecord);

                    if (transactionKafkaDto.getTransactionStatus().equals(TransactionStatus.COMPLETE)) {
                        creditService.makeEnrollment(transactionKafkaDto.getAccountId(), transactionKafkaDto.getMoney());
                    }
                }

                IdempotencyKey idempotencyKey = IdempotencyKey.builder().id(eventTransactionDto.getId()).build();
                idempotencyKeyRepository.save(idempotencyKey);

                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            error = true;
            errorMessage = e.getMessage();
            log.error("Exception while processing withdraw event", e);
        } finally {
            monitoringEventPublisher.publish(monitoringEventPublisher.metric(
                    trace,
                    MonitoringEventPublisher.KAFKA_CONSUMER,
                    "CONSUME",
                    null,
                    eventTransactionDto.getDestination(),
                    System.currentTimeMillis() - start,
                    error ? 500 : 200,
                    error,
                    errorMessage
            ));
            TraceContext.clear();
        }
    }

    private boolean isCompletedCreditCreation(TransactionKafkaDto transactionKafkaDto) {
        return transactionKafkaDto.getTransactionStatus().equals(TransactionStatus.COMPLETE)
                && transactionKafkaDto.getTransactionType().equals(TransactionType.ENROLLMENT)
                && transactionKafkaDto.getSourceId() != null
                && creditTempRepository.existsById(transactionKafkaDto.getSourceId());
    }

    private boolean isCreditPayback(TransactionKafkaDto transactionKafkaDto) {
        return transactionKafkaDto.getTransactionType().equals(TransactionType.WITHDRAWAL)
                && transactionKafkaDto.getSourceId() != null
                && paymentHistoryRecordRepository.existsById(transactionKafkaDto.getSourceId());
    }

    private void createCreditFromTemp(TransactionKafkaDto transactionKafkaDto) {
        CreditTemp creditTemp = creditTempRepository.findById(transactionKafkaDto.getSourceId()).orElseThrow();

        if (!creditRepository.existsByCardAccount(creditTemp.getCardAccount())) {
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
        }

        creditTempRepository.deleteById(creditTemp.getId());
    }
}
