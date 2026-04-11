package org.ricramiel.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.enums.Role;
import org.ricramiel.common.exceptions.status_code_exceptions.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    public UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof String id) {
            try {
                return UUID.fromString(id);
            }
            catch (Exception e) {
                throw new UnauthorizedException("Invalid user id");
            }
        }

        throw new IllegalStateException("Invalid authentication type");
    }

    public Set<Role> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> Role.valueOf(auth.substring(5)))
                .collect(Collectors.toSet());
    }
}