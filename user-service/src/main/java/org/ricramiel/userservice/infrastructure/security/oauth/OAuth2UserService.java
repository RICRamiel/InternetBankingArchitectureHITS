package org.ricramiel.userservice.infrastructure.security.oauth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.userservice.api.mappers.RolesMapper;
import org.ricramiel.common.enums.Role;
import org.ricramiel.userservice.domain.models.entities.OAuth2Provider;
import org.ricramiel.userservice.domain.models.entities.User;
import org.ricramiel.userservice.domain.models.entities.ids.OAuth2ProviderId;
import org.ricramiel.userservice.infrastructure.exceptions.AuthException;
import org.ricramiel.userservice.infrastructure.repositories.OAuth2ProviderRepository;
import org.ricramiel.userservice.infrastructure.repositories.UserRepository;
import org.ricramiel.userservice.infrastructure.security.oauth.user_info.OAuth2UserInfo;
import org.ricramiel.userservice.infrastructure.security.oauth.user_info.OAuth2UserInfoFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RolesMapper rolesMapper;
    private final OAuth2ProviderRepository oAuth2ProviderRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String providerId = userRequest.getClientRegistration().getRegistrationId();
        return getOrCreateUser(providerId, oauth2User);
    }

    private CustomOauth2User getOrCreateUser(String providerId, OAuth2User oauth2User) {
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                providerId,
                oauth2User.getAttributes()
        );

        Optional<OAuth2Provider> optionalProvider = oAuth2ProviderRepository.findById(
                new OAuth2ProviderId(userInfo.getId(), providerId)
        );

        if (optionalProvider.isPresent()) {
            User user = optionalProvider.get().getUser();
            return new CustomOauth2User(user, oauth2User.getAttributes(), rolesMapper);
        }

        if (userRepository.existsByEmail(userInfo.getEmail())) {
            throw new AuthException("Email address already in use");
        }

        OAuth2Provider userProviderEntity = OAuth2Provider
                .builder()
                .provider(providerId)
                .providerUserId(userInfo.getId())
                .build();

        User user = User
                .builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .roles(Set.of(Role.CLIENT))
                .isActive(true)
                .build();

        userRepository.save(user);

        log.info("Created new user with oauth2 provider {} and email {}", providerId, userInfo.getEmail());

        userProviderEntity.setUser(user);
        oAuth2ProviderRepository.save(userProviderEntity);

        return new CustomOauth2User(user, oauth2User.getAttributes(), rolesMapper);
    }
}