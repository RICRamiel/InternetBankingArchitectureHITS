package org.ricramiel.creditservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventDto;
import org.ricramiel.common.dtos.EventWithdrawDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.creditservice.infrastructure.CreditServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final CreditServiceImpl creditService;
    @KafkaListener(topics = "${app.kafka.topics.withdraw}", groupId = "zxc")
    public void listenWithAck(EventWithdrawDto eventWithdrawDto, Acknowledgment acknowledgment) {
        try {
            WithdrawDto withdrawDto = eventWithdrawDto.getData();
            if (!Objects.equals(withdrawDto.getDestination(), "credit")){
                log.error("Received unexpected withdraw event with destination {}", withdrawDto.getDestination());
                acknowledgment.acknowledge();
            }
            creditService.makeEnrollment(withdrawDto.getCardAccountId(), withdrawDto.getSum());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Exception while processing withdraw event", e);
        }
    }
}
