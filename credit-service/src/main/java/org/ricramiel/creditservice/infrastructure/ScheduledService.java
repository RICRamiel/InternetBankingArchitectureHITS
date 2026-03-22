package org.ricramiel.creditservice.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.model.CreditRule;
import org.ricramiel.creditservice.model.OutboxEvent;
import org.ricramiel.creditservice.model.PaymentHistoryRecord;
import org.ricramiel.creditservice.repository.CreditRepository;
import org.ricramiel.creditservice.repository.OutboxRepository;
import org.ricramiel.creditservice.service.CreditService;
import org.ricramiel.creditservice.service.PaymentHistoryRecordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledService {

    private final CreditService creditService;
    private final CreditRepository creditRepository;
    private final PaymentHistoryRecordService paymentHistoryRecordService;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.withdraw}")
    private String TRANSACTION_WITHDRAW;

    @Value("${type.withdraw}")
    private String TYPE;

    @Scheduled(fixedRate = 120_000)
    @Transactional
    protected void interestUpdater() {
        int size = 100;
        int pageNumber = 0;
        Page<Credit> credits = creditService.findAllPageable(pageNumber, size);

        while (!credits.isEmpty()) {
            credits.forEach(credit -> {
                CreditRule creditRule = credit.getCreditRule();

                long iterationsAmount = Duration.between(credit.getLastInterestUpdate(), LocalDateTime.now()).getSeconds() / creditRule.getCollectionPeriodSeconds();


                for (int i = 0; i < iterationsAmount; i++) {
                    credit.setLastInterestUpdate(LocalDateTime.now());
                    credit.setInterestDebtSum(credit.getInterestDebtSum().add(credit.getCurrentDebtSum().multiply(creditRule.getPercentage().divide(new BigDecimal(100), RoundingMode.CEILING))));
                }


                creditRepository.save(credit);
            });
            pageNumber++;
            credits = creditService.findAllPageable(pageNumber, size);
        }
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    protected void moneyCall() {
        int size = 100;
        int pageNumber = 0;
        Page<Credit> credits = creditService.findAllPageable(pageNumber, size);

        while (!credits.isEmpty()) {
            credits.forEach(credit -> {

                if (credit.getInterestDebtSum().compareTo(BigDecimal.ZERO) > 0) {
                    withdraw(credit.getCardAccount(), credit.getInterestDebtSum());
                }
            });
            pageNumber++;
            credits = creditService.findAllPageable(pageNumber, size);
        }
    }

    @Transactional
    public void withdraw(UUID cardAccountId, BigDecimal money) {
        PaymentHistoryRecord paymentHistoryRecord = PaymentHistoryRecord.builder()
                .sum(money)
                .date(LocalDateTime.now())
                .cardAccount(cardAccountId)
                .transactionStatus(TransactionStatus.IN_PROGRESS)
                .currency(creditRepository.findByCardAccount(cardAccountId).getCurrency())
                .build();

        paymentHistoryRecord = paymentHistoryRecordService.createHistoryRecord(paymentHistoryRecord);

        TransactionKafkaDto transactionKafkaDto = new TransactionKafkaDto(
                paymentHistoryRecord.getId(),
                cardAccountId,
                paymentHistoryRecord.getDate(),
                TransactionType.WITHDRAWAL,
                paymentHistoryRecord.getTransactionStatus(),
                "Погашение кредита",
                money,
                paymentHistoryRecord.getCurrency());

        sendToKafka(transactionKafkaDto);
    }

    @SneakyThrows
    @Transactional
    public void sendToKafka(TransactionKafkaDto transactionKafkaDto) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(TRANSACTION_WITHDRAW);
        EventTransactionDto eventTransactionDto = new EventTransactionDto();
        eventTransactionDto.setCreationDate(LocalDateTime.now());
        eventTransactionDto.setData(transactionKafkaDto);
        eventTransactionDto.setId(UUID.randomUUID());
        eventTransactionDto.setDestination("credit");
        outboxEvent.setPayload(objectMapper.writeValueAsString(eventTransactionDto));
        outboxRepository.save(outboxEvent);
    }
}
