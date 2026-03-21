package org.ricramiel.creditservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.*;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.creditservice.infrastructure.CreditServiceImpl;
import org.ricramiel.creditservice.model.PaymentHistoryRecord;
import org.ricramiel.creditservice.repository.PaymentHistoryRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final PaymentHistoryRecordRepository paymentHistoryRecordRepository;

    @Value("${app.kafka.destination}")
    private String type;

    private final CreditServiceImpl creditService;

    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.withdraw}", groupId = "withdraw")
    public void listenWithAck(@Payload EventTransactionDto eventTransactionDto, Acknowledgment acknowledgment) {
        try {

            TransactionKafkaDto transactionKafkaDto = eventTransactionDto.getData();

            PaymentHistoryRecord paymentHistoryRecord = paymentHistoryRecordRepository.findById(transactionKafkaDto.getSourceId()).orElseThrow();
            paymentHistoryRecord.setTransactionStatus(transactionKafkaDto.getTransactionStatus());
            paymentHistoryRecordRepository.save(paymentHistoryRecord);

            if (!Objects.equals(eventTransactionDto.getDestination(), type)){
                log.error("Received unexpected withdraw event with destination {}", eventTransactionDto.getDestination());
                acknowledgment.acknowledge();
                return;
            }

            if(transactionKafkaDto.getTransactionStatus().equals(TransactionStatus.COMPLETE)){
                creditService.makeEnrollment(transactionKafkaDto.getAccountId(), transactionKafkaDto.getMoney());
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Exception while processing withdraw event", e);
        }
    }
}
