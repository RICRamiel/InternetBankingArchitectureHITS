package org.ricramiel.creditservice.repository;

import org.ricramiel.creditservice.model.Credit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CreditRepository extends JpaRepository<Credit, UUID> {
    List<Credit> findByUserId(UUID userId);


    Credit findByCardAccount(UUID cardAccountId);

}
