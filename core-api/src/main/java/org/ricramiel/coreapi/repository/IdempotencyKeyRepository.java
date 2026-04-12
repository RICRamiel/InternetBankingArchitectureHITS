package org.ricramiel.coreapi.repository;

import org.ricramiel.coreapi.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {
}
