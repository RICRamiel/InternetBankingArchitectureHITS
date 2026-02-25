package org.ricramiel.coreapi.repository;

import org.ricramiel.coreapi.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Integer> {
}
