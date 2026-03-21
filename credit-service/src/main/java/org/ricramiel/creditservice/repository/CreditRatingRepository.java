package org.ricramiel.creditservice.repository;

import org.ricramiel.creditservice.model.CreditRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditRatingRepository extends JpaRepository<CreditRating, UUID> {
    CreditRating findByUserId(UUID userId);
}
