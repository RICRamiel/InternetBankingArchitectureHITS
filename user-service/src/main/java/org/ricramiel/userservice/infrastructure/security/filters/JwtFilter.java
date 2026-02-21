package org.ricramiel.userservice.infrastructure.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.headers.CustomHeaders;
import org.ricramiel.userservice.api.mappers.RolesMapper;
import org.ricramiel.common.enums.Role;
import org.ricramiel.userservice.infrastructure.services.JwtAccessTokenUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private final RolesMapper rolesMapper;
    private final JwtAccessTokenUtil accessTokenService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {
        String token = getTokenFromRequestOrNull(request);
        SetContextAuthenticationAndAddHeaderIfValid(token, response);
        chain.doFilter(request, response);
    }

    private void SetContextAuthenticationAndAddHeaderIfValid(String token, HttpServletResponse response) {
        if (token == null){
            log.debug("Token is not present");
            return;
        }
        String error = accessTokenService.getValidationErrorMessageOrNull(token);
        if (error != null) {
            log.warn("Token is present, but not valid. Token: {}, cause: {}", token, error);
            return;
        }

        Set<Role> roles = accessTokenService.extractRoles(token);
        UUID id = accessTokenService.extractId(token);
        List<SimpleGrantedAuthority> authorities = rolesMapper.toAuthorities(roles);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(token, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        log.debug("Token is valid and security context holder is filled");

        String rolesString = roles.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
        response.addHeader(CustomHeaders.USER_ROLES_HEADER, rolesString);
        response.addHeader(CustomHeaders.USER_ID_HEADER, id.toString());
        log.debug("Response auth headers are filled");
    }

    private String getTokenFromRequestOrNull(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}