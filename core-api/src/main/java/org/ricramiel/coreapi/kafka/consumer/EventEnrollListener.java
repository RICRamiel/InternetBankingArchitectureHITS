package org.ricramiel.coreapi.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.EventEnrollDto;
import org.ricramiel.common.dtos.EventWithdrawDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.coreapi.service.implementation.CardAccountServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventEnrollListener {
    @Value("${app.kafka.destination}")
    private String destination;

    private final CardAccountServiceImpl cardAccountService;
    @KafkaListener(topics = "${app.kafka.topics.enroll}", groupId = "enroll")
    public void listenWithAck(@Payload EventEnrollDto eventEnrollDto, Acknowledgment acknowledgment) {
        try {
            EnrollDto enrollDto = eventEnrollDto.getData();
            if (!Objects.equals(enrollDto.getDestination(), destination)){
                log.error("Received unexpected enroll event with destination {}", enrollDto.getDestination());
                acknowledgment.acknowledge();
            }
            cardAccountService.enroll(enrollDto);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Exception while processing enroll event", e);
        }
    }
}
