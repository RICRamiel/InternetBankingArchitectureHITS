package org.ricramiel.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ricramiel.notificationservice.dto.FcmTokenRequest;
import org.ricramiel.notificationservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final NotificationService notificationService;

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> registerToken(@Valid @RequestBody FcmTokenRequest request) {
        notificationService.registerFcmToken(request);
        return ResponseEntity.ok(Map.of("status", "success", "message", "FCM token registered"));
    }

    @DeleteMapping("/token")
    public ResponseEntity<Map<String, String>> unregisterToken(@RequestParam String token) {
        notificationService.unregisterFcmToken(token);
        return ResponseEntity.ok(Map.of("status", "success", "message", "FCM token removed"));
    }
}