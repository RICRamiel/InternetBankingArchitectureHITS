package org.ricramiel.coreapi.repository;

import org.ricramiel.coreapi.entity.CardAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardAccountRepository extends JpaRepository<CardAccount, UUID>, PagingAndSortingRepository<CardAccount, UUID> {
    Page<CardAccount> findByUserId(UUID userId, Pageable pageable);
}
