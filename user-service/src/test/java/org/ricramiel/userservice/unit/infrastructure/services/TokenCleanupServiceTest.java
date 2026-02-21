package org.ricramiel.userservice.unit.infrastructure.services;

import org.ricramiel.userservice.infrastructure.repositories.RefreshTokenRepository;
import org.ricramiel.userservice.infrastructure.services.TokenCleanupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenCleanupServiceTest {

    @Mock
    private RefreshTokenRepository tokenRepository;

    @InjectMocks
    private TokenCleanupService tokenCleanupService;

    @Test
    void cleanupExpiredTokens_shouldDeleteExpiredTokens() {
        tokenCleanupService.cleanupExpiredTokens();

        verify(tokenRepository).deleteAllByExtractedExpiryDateBefore(any(Date.class));
    }
}