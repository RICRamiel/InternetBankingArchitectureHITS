package org.ricramiel.creditservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.EventDto;
import org.ricramiel.common.dtos.EventWithdrawDto;
import org.ricramiel.creditservice.infrastructure.CreditServiceImpl;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final CreditServiceImpl creditService;
    @KafkaListener(topics = "transactionWithdraw", groupId = "zxc")
    public void listenWithAck(EventWithdrawDto eventWithdrawDto, Acknowledgment acknowledgment) {
        try {
            creditService.makeEnrollment(eventWithdrawDto.getData().getCardAccountId(),eventWithdrawDto.getData().getSum());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            System.err.println("Ошибка обработки: " + e.getMessage());
            // Не подтверждаем - сообщение будет обработано снова
        }
    }
}
