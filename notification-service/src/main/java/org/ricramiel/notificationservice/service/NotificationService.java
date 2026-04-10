package org.ricramiel.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.notificationservice.dto.SseOperationPayload;
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

    private final NotificationRepository notificationRepository;
    private final AccountUserMappingRepository mappingRepository;

    public boolean isEventAlreadyProcessed(UUID eventId) {
        return notificationRepository.existsByEventId(eventId);
    }

    @Transactional
    public UUID saveToHistoryAndGetUserId(UUID eventId, UUID accountId, SseOperationPayload payload) {
        AccountUserMapping mapping = mappingRepository.findByAccountId(accountId);

        if (mapping == null) {
            log.error("Mapping for account {} not found in local DB.", accountId);
            throw new RuntimeException("Account mapping not found");
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

        notificationRepository.save(notification);
        return userId;
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
}