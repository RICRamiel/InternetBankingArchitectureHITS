package org.ricramiel.notificationservice.controller;

import lombok.AllArgsConstructor;
import org.ricramiel.common.enums.Role;
import org.ricramiel.notificationservice.entity.Notification;
import org.ricramiel.notificationservice.entity.SseConnection;
import org.ricramiel.notificationservice.service.CurrentUserService;
import org.ricramiel.notificationservice.service.NotificationService;
import org.ricramiel.notificationservice.service.SseEmitterStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@AllArgsConstructor
public class NotificationSSEController {
    private final SseEmitterStore emitterStore;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        UUID userId = currentUserService.getUserId();
        Set<Role> roles = currentUserService.getRoles();

        SseEmitter emitter = new SseEmitter(3600_000L);
        SseConnection connection = new SseConnection(emitter, userId, roles);

        emitterStore.addConnection(connection);

        Runnable cleanup = () -> emitterStore.removeConnection(connection);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }

    @GetMapping("/all")
    public List<Notification> getAllNotifications() {
        UUID userId = currentUserService.getUserId();
        return notificationService.getAllNotifications(userId);
    }

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications() {
        UUID userId = currentUserService.getUserId();
        return notificationService.getUnreadNotifications(userId);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable UUID id) {
        UUID userId = currentUserService.getUserId();
        notificationService.markAsRead(id, userId);
    }
}
