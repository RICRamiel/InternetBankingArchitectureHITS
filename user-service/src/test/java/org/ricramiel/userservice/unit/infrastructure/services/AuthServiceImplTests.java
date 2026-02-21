package org.ricramiel.userservice.unit.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import org.ricramiel.common.enums.Role;
import org.ricramiel.userservice.domain.models.entities.RefreshToken;
import org.ricramiel.userservice.domain.models.entities.User;
import org.ricramiel.userservice.domain.models.requests.LoginRequestModel;
import org.ricramiel.userservice.domain.models.requests.RegisterRequestModel;
import org.ricramiel.userservice.domain.models.responses.JwtModel;
import org.ricramiel.userservice.infrastructure.exceptions.AuthException;
import org.ricramiel.userservice.infrastructure.repositories.RefreshTokenRepository;
import org.ricramiel.userservice.infrastructure.repositories.UserRepository;
import org.ricramiel.userservice.infrastructure.services.AuthServiceImpl;
import org.ricramiel.userservice.infrastructure.services.JwtAccessTokenUtil;
import org.ricramiel.userservice.infrastructure.services.JwtRefreshTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtAccessTokenUtil accessTokenUtil;

    @Mock
    private JwtRefreshTokenUtil refreshTokenUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private UUID testId;
    private User testUser;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUser = User.builder()
                .id(testId)
                .email("test@ricramiel.com")
                .name("Test User")
                .roles(Set.of(Role.USER))
                .password("password")
                .isActive(true)
                .build();
    }

    @Test
    void revoke_ShouldForgetToken() {
        String refreshTokenValue = "refresh";
        UUID refreshTokenId = UUID.randomUUID();
        RefreshToken refreshToken = RefreshToken
                .builder()
                .value(refreshTokenValue)
                .id(refreshTokenId)
                .user(testUser)
                .build();
        when(refreshTokenRepository.findByValue(refreshTokenValue)).thenReturn(Optional.of(refreshToken));

        authService.revoke(refreshTokenValue);

        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void login_ShouldReturnTokens() {
        when(passwordEncoder.matches("password", "password")).thenReturn(true);
        when(userRepository.findByEmailAndIsActiveIsTrue("test@ricramiel.com")).thenReturn(Optional.of(testUser));
        when(accessTokenUtil.generateToken(any(User.class))).thenReturn("access");
        when(refreshTokenUtil.generateToken(any(User.class))).thenReturn("refresh");

        JwtModel result = authService.login(new LoginRequestModel("test@ricramiel.com", "password"));

        assertThat(result)
                .hasFieldOrPropertyWithValue("accessToken", "access")
                .hasFieldOrPropertyWithValue("refreshToken", "refresh");
    }

    @Test
    void login_WithWrongPassword_ShouldThrowException() {
        when(passwordEncoder.matches("123", "password")).thenReturn(false);
        when(userRepository.findByEmailAndIsActiveIsTrue("test@ricramiel.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(new LoginRequestModel("test@ricramiel.com", "123")))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void login_WithNonExistingEmail_ShouldThrowException() {
        when(userRepository.findByEmailAndIsActiveIsTrue("test@ricramiel.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequestModel("test@ricramiel.com", "123")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        when(userRepository.existsByEmail("test@ricramiel.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequestModel("Test Name", "test@ricramiel.com", "123"))
        ).isInstanceOf(AuthException.class);
    }

    @Test
    void register_ShouldReturnTokens() {
        when(passwordEncoder.encode("123")).thenReturn("123");
        when(userRepository.existsByEmail("test@ricramiel.com")).thenReturn(false);
        when(accessTokenUtil.generateToken(any(User.class))).thenReturn("access");
        when(refreshTokenUtil.generateToken(any(User.class))).thenReturn("refresh");

        JwtModel result = authService.register(new RegisterRequestModel("Test Name", "test@ricramiel.com", "123"));

        assertThat(result)
                .hasFieldOrPropertyWithValue("accessToken", "access")
                .hasFieldOrPropertyWithValue("refreshToken", "refresh");
    }

    @Test
    void refreshAndRotate_ShouldReturnDifferentTokens() {
        RefreshToken refreshTokenValue = RefreshToken
                .builder()
                .value("refresh")
                .id(UUID.randomUUID())
                .user(testUser)
                .userId(testId)
                .build();
        when(refreshTokenRepository.findByValue("refresh")).thenReturn(Optional.of(refreshTokenValue));

        JwtModel result = authService.refreshAndRotate("refresh");

        assertThat(result.getRefreshToken()).isNotEqualTo("refresh");
        verify(refreshTokenRepository, times(1)).save(any());
    }

    @Test
    void refreshAndRotate_WithNonExistingRefreshToken_ShouldThrowException() {
        when(refreshTokenRepository.findByValue("refresh")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshAndRotate("refresh")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createAndSaveJwtToken_ShouldReturnTokensAndSaveToDb() {
        when(accessTokenUtil.generateToken(any(User.class))).thenReturn("access");
        when(refreshTokenUtil.generateToken(any(User.class))).thenReturn("refresh");

        JwtModel result = authService.createAndSaveJwtToken(testUser);

        verify(refreshTokenRepository).save(argThat(token ->
                token.getUser().equals(testUser) && token.getValue().equals("refresh")
        ));
        assertThat(result.getAccessToken()).isEqualTo("access");
        assertThat(result.getRefreshToken()).isEqualTo("refresh");
    }
}
