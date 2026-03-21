package org.ricramiel.preferencesservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.preferencesservice.dtos.UserPreferencesDto;
import org.ricramiel.preferencesservice.models.UserPreferences;
import org.ricramiel.preferencesservice.repositories.UserPreferencesRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreferencesService {

    private final UserPreferencesRepository repository;

    public UserPreferencesDto getPreferences(UUID userId) {
        log.debug("Fetching preferences for user: {}", userId);
        
        return repository.findById(userId)
                .map(this::toDto)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    @Transactional
    public UserPreferencesDto updatePreferences(UUID userId, UserPreferencesDto dto) {
        log.debug("Updating preferences for user: {}", userId);
        
        UserPreferences preferences = repository.findById(userId)
                .orElseGet(() -> UserPreferences.builder()
                        .userId(userId)
                        .build());
        
        preferences.setTheme(dto.getTheme());
        preferences.setHiddenAccounts(dto.getHiddenAccounts());
        
        return toDto(repository.save(preferences));
    }

    private UserPreferencesDto createDefaultPreferences(UUID userId) {
        log.info("Creating default preferences for user: {}", userId);
        
        UserPreferences defaultPreferences = UserPreferences.builder()
                .userId(userId)
                .theme("LIGHT")
                .hiddenAccounts(new java.util.HashSet<>())
                .build();
        
        return toDto(repository.save(defaultPreferences));
    }

    private UserPreferencesDto toDto(UserPreferences entity) {
        return UserPreferencesDto.builder()
                .theme(entity.getTheme())
                .hiddenAccounts(entity.getHiddenAccounts())
                .build();
    }
}