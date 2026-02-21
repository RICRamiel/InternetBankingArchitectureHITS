package org.ricramiel.userservice.unit.infrastructure.security.oauth;

import org.ricramiel.common.enums.Role;
import org.ricramiel.userservice.api.mappers.RolesMapper;
import org.ricramiel.userservice.domain.models.entities.OAuth2Provider;
import org.ricramiel.userservice.domain.models.entities.User;
import org.ricramiel.userservice.infrastructure.exceptions.AuthException;
import org.ricramiel.userservice.infrastructure.repositories.OAuth2ProviderRepository;
import org.ricramiel.userservice.infrastructure.repositories.UserRepository;
import org.ricramiel.userservice.infrastructure.security.oauth.CustomOauth2User;
import org.ricramiel.userservice.infrastructure.security.oauth.OAuth2UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;


@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2ProviderRepository oAuth2ProviderRepository;

    @Mock
    private OAuth2User oauth2User;

    @InjectMocks
    private OAuth2UserService oAuth2UserService;

    private final String TEST_PROVIDER = "google";
    private final String TEST_USER_ID = "oauth2-user-id";
    private final String TEST_EMAIL = "user@ricramiel.com";
    private final String TEST_NAME = "Test User";

    @Test
    void getOrCreateUser_shouldReturnExistingUser() {
        Map<String, Object> attributes = Map.of(
                "sub", TEST_USER_ID,
                "email", TEST_EMAIL,
                "name", TEST_NAME
        );
        when(oauth2User.getAttributes()).thenReturn(attributes);

        OAuth2Provider provider = mock(OAuth2Provider.class);
        User existingUser = User.builder()
                .email(TEST_EMAIL)
                .roles(Set.of(Role.USER))
                .build();
        when(provider.getUser()).thenReturn(existingUser);
        when(oAuth2ProviderRepository.findById(any())).thenReturn(Optional.of(provider));

        CustomOauth2User result = ReflectionTestUtils.invokeMethod(
                oAuth2UserService,
                "getOrCreateUser",
                TEST_PROVIDER,
                oauth2User
        );

        assertNotNull(result);
        assertEquals(existingUser, result.getUser());
        verify(oAuth2ProviderRepository, never()).save(any());
    }

    @Test
    void getOrCreateUser_shouldCreateNewUserWhenNotExists() {
        Map<String, Object> attributes = Map.of(
                "sub", TEST_USER_ID,
                "email", TEST_EMAIL,
                "name", TEST_NAME
        );
        when(oauth2User.getAttributes()).thenReturn(attributes);
        when(oAuth2ProviderRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(oAuth2ProviderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomOauth2User result = ReflectionTestUtils.invokeMethod(
                oAuth2UserService,
                "getOrCreateUser",
                TEST_PROVIDER,
                oauth2User
        );

        assertNotNull(result);
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(TEST_EMAIL) &&
                        user.getName().equals(TEST_NAME) &&
                        user.getRoles().contains(Role.USER) &&
                        user.isActive()
        ));
        verify(oAuth2ProviderRepository).save(argThat(provider ->
                provider.getProvider().equals(TEST_PROVIDER) &&
                        provider.getProviderUserId().equals(TEST_USER_ID)
        ));
    }

    @Test
    void getOrCreateUser_shouldThrowWhenEmailExists() {
        Map<String, Object> attributes = Map.of(
                "sub", TEST_USER_ID,
                "email", TEST_EMAIL,
                "name", TEST_NAME
        );
        when(oauth2User.getAttributes()).thenReturn(attributes);
        when(oAuth2ProviderRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        assertThrows(AuthException.class, () ->
                ReflectionTestUtils.invokeMethod(
                        oAuth2UserService,
                        "getOrCreateUser",
                        TEST_PROVIDER,
                        oauth2User
                ));
    }
}