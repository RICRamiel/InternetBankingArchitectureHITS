package org.ricramiel.creditservice.configurations;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.headers.security.SecurityHeadersPropagationFilter;
import org.ricramiel.common.headers.security.SetSecurityContextFromHeadersFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled=true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final SecurityHeadersPropagationFilter securityHeadersPropagationFilter;
    private final SetSecurityContextFromHeadersFilter setSecurityContextFromHeadersFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(setSecurityContextFromHeadersFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(securityHeadersPropagationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
}