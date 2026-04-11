package org.ricramiel.notificationservice.repository;

import org.ricramiel.notificationservice.entity.AccountUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountUserMappingRepository extends JpaRepository<AccountUserMapping, UUID> {
    AccountUserMapping findByAccountId(UUID accountId);
}