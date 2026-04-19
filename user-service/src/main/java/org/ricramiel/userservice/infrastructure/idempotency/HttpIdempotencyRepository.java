package org.ricramiel.userservice.infrastructure.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HttpIdempotencyRepository extends JpaRepository<HttpIdempotencyEntity, UUID> {
    Optional<HttpIdempotencyEntity> findByIdempotencyKeyAndUserScope(String idempotencyKey, String userScope);
}
