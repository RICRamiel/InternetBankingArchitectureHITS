package org.ricramiel.notificationservice.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.notificationservice.entity.Notification;
import org.ricramiel.notificationservice.entity.UserFcmToken;
import org.ricramiel.notificationservice.repository.UserFcmTokenRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final UserFcmTokenRepository tokenRepository;

    private static final int FCM_BATCH_SIZE = 500;

    /**
     * Сохранение или обновление токена (Upsert).
     * Если браузер генерирует токен, который уже был у другого юзера - перепривязываем.
     */
    @Transactional
    public void saveOrUpdateToken(UUID userId, String token, String platform) {
        tokenRepository.findByToken(token).ifPresentOrElse(
                existingToken -> {
                    if (!existingToken.getUserId().equals(userId) || !existingToken.getPlatform().equals(platform)) {
                        existingToken.setUserId(userId);
                        existingToken.setPlatform(platform);
                        tokenRepository.save(existingToken);
                        log.info("Reassigned existing FCM token to user: {}, platform: {}", userId, platform);
                    }
                },
                () -> {
                    tokenRepository.save(UserFcmToken.builder()
                            .userId(userId)
                            .token(token)
                            .platform(platform)
                            .build());
                    log.info("Saved new FCM token for user: {}, platform: {}", userId, platform);
                }
        );
    }

    /**
     * Удаление токена (например, при логауте)
     */
    @Transactional
    public void removeToken(String token) {
        tokenRepository.findByToken(token).ifPresent(t -> {
            tokenRepository.delete(t);
            log.info("Removed FCM token");
        });
    }

    /**
     * Отправка push-уведомления КОНКРЕТНОМУ пользователю (на все его устройства)
     */
    @Async
    public void sendToUser(UUID userId, Notification notification, String targetPlatform) {
        List<UserFcmToken> userTokens = tokenRepository.findAllByUserId(userId).stream()
                .filter(t -> t.getPlatform().equals(targetPlatform)) // Отправляем только на нужную панель
                .toList();

        if (userTokens.isEmpty()) {
            log.debug("No FCM tokens found for user: {} on platform: {}", userId, targetPlatform);
            return;
        }

        List<String> tokens = userTokens.stream().map(UserFcmToken::getToken).toList();
        sendBatchPush(tokens, notification);
    }

    /**
     * Массовая рассылка push-уведомлений ВСЕМ работникам (на панель worker.bank.su)
     */
    @Async
    public void sendToAllWorkers(Notification notification) {
        List<UserFcmToken> workerTokens = tokenRepository.findAllByPlatform("WEB_WORKER");

        if (workerTokens.isEmpty()) {
            log.debug("No active worker FCM tokens found.");
            return;
        }

        List<String> tokens = workerTokens.stream().map(UserFcmToken::getToken).toList();
        log.info("Sending broadcast push to {} workers", tokens.size());
        sendBatchPush(tokens, notification);
    }

    /**
     * Внутренний метод пакетной отправки (разбивает список > 500 на части)
     */
    private void sendBatchPush(List<String> tokens, Notification notification) {
        for (int i = 0; i < tokens.size(); i += FCM_BATCH_SIZE) {
            List<String> batch = tokens.subList(i, Math.min(i + FCM_BATCH_SIZE, tokens.size()));

            // Формируем данные для Web Push
            Map<String, String> data = Map.of(
                    "notificationId", notification.getId().toString(),
                    "type", notification.getType() != null ? notification.getType() : "INFO",
                    "amount", notification.getAmount() != null ? notification.getAmount().toString() : "",
                    "currency", notification.getCurrency() != null ? notification.getCurrency() : ""
            );

            // Заголовок и тело для системной шторки уведомлений ОС/Браузера
            com.google.firebase.messaging.Notification firebaseNotif =
                    com.google.firebase.messaging.Notification.builder()
                            .setTitle("Банк: " + notification.getType())
                            .setBody(notification.getMessage())
                            .build();

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batch)
                    .setNotification(firebaseNotif)
                    .putAllData(data)
                    .build();

            try {
                BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
                log.info("FCM Batch sent. Success: {}, Failure: {}",
                        response.getSuccessCount(), response.getFailureCount());

                // Очистка невалидных токенов (если юзер удалил данные или запретил уведомления)
                cleanupInvalidTokens(batch, response);

            } catch (FirebaseMessagingException e) {
                log.error("Error sending FCM batch: {}", e.getMessage());
            }
        }
    }

    /**
     * Удаляет токены, которые Firebase признал невалидными (NotRegistered, InvalidRegistration)
     */
    private void cleanupInvalidTokens(List<String> tokens, BatchResponse response) {
        for (int i = 0; i < response.getResponses().size(); i++) {
            SendResponse r = response.getResponses().get(i);
            if (!r.isSuccessful() && r.getException() != null) {
                String errorCode = r.getException().getMessagingErrorCode().toString();
                if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
                    String deadToken = tokens.get(i);
                    tokenRepository.findByToken(deadToken).ifPresent(tokenRepository::delete);
                    log.warn("Cleaned up invalid FCM token: {}...", deadToken.substring(0, Math.min(10, deadToken.length())));
                }
            }
        }
    }
}