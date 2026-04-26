package org.ricramiel.common.tracing;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnClass(KafkaTemplate.class)
public class MonitoringKafkaConfig {
    @Bean
    @SuppressWarnings("rawtypes")
    public ProducerFactory kafkaProducerFactory(
            @Value("${spring.kafka.bootstrap-servers:${KAFKA_SERVER_IP_PORT:${KAFKA_SERVER_IP:127.0.0.1:9092}}}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public KafkaTemplate kafkaTemplate(ProducerFactory kafkaProducerFactory) {
        return new KafkaTemplate<>(kafkaProducerFactory);
    }
}
