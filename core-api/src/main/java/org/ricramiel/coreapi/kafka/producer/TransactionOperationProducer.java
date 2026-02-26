package org.ricramiel.coreapi.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionOperationProducer {
    @Value("spring.kafka.topic.transaction")
    private String topic;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendTransactionOperation(TransactionOperation transactionOperation) throws JsonProcessingException {
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(transactionOperation));
    }
}
