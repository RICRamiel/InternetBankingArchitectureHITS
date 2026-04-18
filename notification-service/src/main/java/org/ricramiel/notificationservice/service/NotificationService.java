package org.ricramiel.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.notificationservice.dto.FcmTokenRequest;
import org.ricramiel.notificationservice.dto.OperationPayload;
import org.ricramiel.notificationservice.entity.AccountUserMapping;
import org.ricramiel.notificationservice.entity.Notification;
import org.ricramiel.notificationservice.repository.AccountUserMappingRepository;
import org.ricramiel.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final CurrentUserService currentUserService;
    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;
    private final AccountUserMappingRepository mappingRepository;

    public boolean isEventAlreadyProcessed(UUID eventId) {
        return notificationRepository.existsByEventId(eventId);
    }

    @Transactional
    public Notification saveToHistoryAndGetUserId(UUID eventId, UUID accountId, OperationPayload payload) {
        AccountUserMapping mapping = mappingRepository.findByAccountId(accountId);

        if (mapping == null) {
            log.error("Mapping for account {} not found in local DB.", accountId);
            throw new NotFoundException("Account mapping not found");
        }

        UUID userId = mapping.getUserId();

        Notification notification = Notification.builder()
                .eventId(eventId)
                .userId(userId)
                .operationId(payload.getOperationId())
                .type(payload.getType())
                .amount(payload.getAmount())
                .currency(payload.getCurrency())
                .message(payload.getMessage())
                .build();

        return notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getAllNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    /**
     * Управление FCM токенами
     */
    public void registerFcmToken(FcmTokenRequest request) {
        UUID currentUserId = currentUserService.getUserId();
        fcmService.saveOrUpdateToken(currentUserId, request.getToken(), request.getPlatform());
    }

    public void unregisterFcmToken(String token) {
        fcmService.removeToken(token);
    }
}