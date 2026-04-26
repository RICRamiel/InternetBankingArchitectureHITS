package org.ricramiel.monitoringservice.kafka.listener;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.EventMetricDto;
import org.ricramiel.monitoringservice.entity.MetricEntity;
import org.ricramiel.monitoringservice.repository.MetricRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsListener {

    private final MetricRepository metricRepository;

    @KafkaListener(topics = "metrics-topic", groupId = "monitoring-group")
    public void consumeMetric(EventMetricDto dto) {
        MetricEntity entity = new MetricEntity();
        entity.setTime(dto.getTime());
        entity.setTraceId(dto.getTraceId());
        entity.setSpanId(dto.getSpanId());
        entity.setParentSpanId(dto.getParentSpanId());
        entity.setServiceName(dto.getServiceName());
        entity.setOperationType(dto.getOperationType());
        entity.setMethod(dto.getMethod());
        entity.setEndpoint(dto.getEndpoint());
        entity.setTopic(dto.getTopic());
        entity.setDurationMs(dto.getDurationMs());
        entity.setStatusCode(dto.getStatusCode());
        entity.setError(dto.isError());
        entity.setErrorMessage(dto.getErrorMessage());

        metricRepository.save(entity);
    }
}
