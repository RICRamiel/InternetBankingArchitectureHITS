package org.ricramiel.coreapi.repository;

import org.ricramiel.coreapi.entity.OutboxAccountEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxAccountEventRepository extends JpaRepository<OutboxAccountEvent, UUID> {
    @Query(nativeQuery = true, value = "SELECT * FROM outbox_account where status=?1")
    List<OutboxAccountEvent> findAllbyStatus(String status);
}
