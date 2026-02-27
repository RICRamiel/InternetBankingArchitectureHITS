package org.ricramiel.coreapi.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.EventWithdrawDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawOperationProducer {
    @Value("spring.kafka.topic.withdraw_transaction")
    private String topic;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendTransactionOperation(EventWithdrawDto event) throws JsonProcessingException {
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(event));
    }
}
