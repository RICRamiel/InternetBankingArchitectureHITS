package org.ricramiel.creditservice.kafka.consumer;

import org.ricramiel.common.dtos.EventDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageListener {
    @KafkaListener(topics = "transactionWithdraw", groupId = "zxc")
    public void listenWithAck(EventDto eventDto, Acknowledgment acknowledgment) {
        try {
            System.out.println("Получено сообщение: " + message);
            // Обработка сообщения
            acknowledgment.acknowledge(); // Подтверждаем обработку
        } catch (Exception e) {
            System.err.println("Ошибка обработки: " + e.getMessage());
            // Не подтверждаем - сообщение будет обработано снова
        }
    }
}
