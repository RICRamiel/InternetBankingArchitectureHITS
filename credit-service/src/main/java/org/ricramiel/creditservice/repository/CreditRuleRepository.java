package org.ricramiel.creditservice.repository;

import org.ricramiel.creditservice.model.CreditRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditRuleRepository extends JpaRepository<CreditRule, UUID> {
}
