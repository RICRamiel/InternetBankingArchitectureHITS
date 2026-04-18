package org.ricramiel.notificationservice.repository;

import org.ricramiel.notificationservice.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, UUID> {
    List<UserFcmToken> findAllByUserId(UUID userId);
    Optional<UserFcmToken> findByToken(String token);
    List<UserFcmToken> findAllByPlatform(String platform);
}