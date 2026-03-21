package org.ricramiel.transactionservice.repository;

import org.ricramiel.transactionservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Integer>, PagingAndSortingRepository<OutboxEvent, Integer> {
    @Query(nativeQuery = true, value = "SELECT * FROM outbox where status=?1")
    List<OutboxEvent> findAllbyStatus(String status);
}
