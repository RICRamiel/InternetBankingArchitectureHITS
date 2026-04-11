package org.ricramiel.creditservice.repository;

import org.ricramiel.creditservice.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {
}
