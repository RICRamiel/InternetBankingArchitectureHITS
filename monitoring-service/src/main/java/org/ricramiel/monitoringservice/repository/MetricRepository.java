package org.ricramiel.monitoringservice.repository;

import org.ricramiel.monitoringservice.entity.MetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MetricRepository extends JpaRepository<MetricEntity, UUID> {
}
