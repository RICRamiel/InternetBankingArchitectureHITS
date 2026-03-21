package org.ricramiel.preferencesservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.preferencesservice.dtos.UserPreferencesDto;
import org.ricramiel.preferencesservice.services.PreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class PreferencesController {

    private final PreferencesService preferencesService;

    @GetMapping
    @PreAuthorize("@accessChecker.isSelf(#userId)")
    public ResponseEntity<UserPreferencesDto> getPreferences(
            @AuthenticationPrincipal UUID userId) {
        log.debug("GET preferences request for user: {}", userId);
        return ResponseEntity.ok(preferencesService.getPreferences(userId));
    }

    @PutMapping
    @PreAuthorize("@accessChecker.isSelf(#userId)")
    public ResponseEntity<UserPreferencesDto> updatePreferences(
            @AuthenticationPrincipal UUID userId,
            @RequestBody UserPreferencesDto dto) {
        log.debug("PUT preferences request for user: {}", userId);
        return ResponseEntity.ok(preferencesService.updatePreferences(userId, dto));
    }
}