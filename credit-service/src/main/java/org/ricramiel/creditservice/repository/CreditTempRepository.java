package org.ricramiel.creditservice.repository;

import org.ricramiel.creditservice.model.CreditTemp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditTempRepository extends JpaRepository<CreditTemp, UUID> {
}
