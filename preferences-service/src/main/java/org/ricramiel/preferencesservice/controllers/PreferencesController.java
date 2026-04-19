package org.ricramiel.preferencesservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.preferencesservice.dtos.UserPreferencesDto;
import org.ricramiel.preferencesservice.services.CurrentUserService;
import org.ricramiel.preferencesservice.services.PreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/preferences")
@RequiredArgsConstructor
public class PreferencesController {

    private final PreferencesService preferencesService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<UserPreferencesDto> getPreferences() {
        return ResponseEntity.ok(preferencesService.getPreferences(currentUserService.getUserId()));
    }

    @PutMapping
    public ResponseEntity<UserPreferencesDto> updatePreferences(@RequestBody UserPreferencesDto dto) {
        return ResponseEntity.ok(preferencesService.updatePreferences(currentUserService.getUserId(), dto));
    }
}