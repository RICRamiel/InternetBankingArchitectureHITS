package org.ricramiel.common.headers.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.ricramiel.common.headers.CustomHeaders;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityHeadersPropagationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String rolesHeader = request.getHeader(CustomHeaders.USER_ROLES_HEADER);
        String idHeader = request.getHeader(CustomHeaders.USER_ID_HEADER);

        if (authHeader != null) {
            response.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }
        if (rolesHeader != null) {
            response.addHeader(CustomHeaders.USER_ROLES_HEADER, rolesHeader);
        }
        if (idHeader != null) {
            response.addHeader(CustomHeaders.USER_ID_HEADER, idHeader);
        }

        chain.doFilter(request, response);
    }
}