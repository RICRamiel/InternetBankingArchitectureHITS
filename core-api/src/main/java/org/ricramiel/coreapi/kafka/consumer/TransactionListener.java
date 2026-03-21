package org.ricramiel.coreapi.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;
import org.ricramiel.coreapi.service.implementation.CardAccountServiceImpl;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Objects;



/**
 * Главный листенер кафки для транзакций
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionListener {

    private final CardAccountServiceImpl cardAccountService;
    //пока не понимаю почему читает только enroll
    @KafkaListener(topicPattern = "${app.kafka.topics.consumer.enroll}|${app.kafka.topics.consumer.withdraw}", groupId = "transaction")
    public void listenWithAck(@Payload EventTransactionDto eventTransactionDto, Acknowledgment acknowledgment) {
        try {
            TransactionKafkaDto dto = eventTransactionDto.getData();
            log.info("transaction listener received data with destination: {}", dto.getAction());
            if(Objects.equals(dto.getTransactionStatus(), TransactionStatus.IN_PROGRESS)){
                if(Objects.equals(dto.getTransactionType(), TransactionType.ENROLLMENT)){
                    cardAccountService.enroll(dto);
                }
                if(Objects.equals(dto.getTransactionType(), TransactionType.WITHDRAWAL)){
                    cardAccountService.withdraw(dto);
                }
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Exception while processing transaction event", e);
        }
    }
}
