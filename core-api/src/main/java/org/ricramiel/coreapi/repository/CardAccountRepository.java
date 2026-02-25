package org.ricramiel.coreapi.repository;

import org.ricramiel.coreapi.entity.CardAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardAccountRepository extends JpaRepository<CardAccount, UUID> {
}
