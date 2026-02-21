package org.ricramiel.common.feign.headers_propagation;

import org.ricramiel.common.headers.CustomHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Configuration
public class DefaultHeadersPropagationConfig {
    @Bean
    public HeadersPropagationConfig headersPropagationConfig() {
        return new HeadersPropagationConfig(
                List.of(
                        HttpHeaders.AUTHORIZATION,
                        CustomHeaders.USER_ID_HEADER,
                        CustomHeaders.USER_ROLES_HEADER,
                        CustomHeaders.CORRELATION_ID_HEADER
                )
        );
    }
}