package org.ricramiel.transactionservice.repository;

import org.ricramiel.transactionservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Integer>, PagingAndSortingRepository<OutboxEvent, Integer> {
}
