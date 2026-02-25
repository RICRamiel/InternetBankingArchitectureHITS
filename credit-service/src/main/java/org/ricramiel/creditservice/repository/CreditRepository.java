package org.ricramiel.creditservice.repository;

import org.ricramiel.creditservice.model.Credit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditRepository extends JpaRepository<Credit, UUID> {
    List<Credit> findByUserId(UUID userId);


    Credit findByCardAccountId(UUID cardAccountId);


    Page<Credit> findAllPageable(PageRequest of);
}
