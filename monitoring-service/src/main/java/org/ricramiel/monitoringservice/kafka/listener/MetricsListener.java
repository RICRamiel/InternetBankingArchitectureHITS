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
        entity.setServiceName(dto.getServiceName());
        entity.setEndpoint(dto.getEndpoint());
        entity.setDurationMs(dto.getDurationMs());
        entity.setStatusCode(dto.getStatusCode());
        entity.setError(dto.isError());

        metricRepository.save(entity);
    }
}
