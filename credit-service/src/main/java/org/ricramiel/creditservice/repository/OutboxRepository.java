package org.ricramiel.creditservice.repository;

import org.ricramiel.creditservice.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Integer>, PagingAndSortingRepository<OutboxEvent, Integer> {
}
