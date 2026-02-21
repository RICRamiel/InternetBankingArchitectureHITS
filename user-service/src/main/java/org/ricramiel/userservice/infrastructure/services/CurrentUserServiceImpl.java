package org.ricramiel.userservice.infrastructure.services;

import lombok.RequiredArgsConstructor;
import org.ricramiel.userservice.domain.services.CurrentUserService;
import org.ricramiel.userservice.infrastructure.repositories.UserRepository;
import org.ricramiel.userservice.domain.models.entities.User;
import org.ricramiel.userservice.infrastructure.exceptions.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserRepository userRepository;
    private final JwtAccessTokenUtil accessTokenUtil;

    @Override
    public User getUser() {
        return userRepository
                .findByEmail(getEmail())
                .orElseThrow(() -> new AuthException("Unauthorized"));
    }

    @Override
    public String getEmail() {
        return accessTokenUtil.extractEmail(getToken());
    }

    @Override
    public UUID getId() {
        return accessTokenUtil.extractId(getToken());
    }

    private String getToken(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof String jwt) {
            return jwt;
        }

        throw new IllegalStateException("Invalid authentication type");
    }
}
