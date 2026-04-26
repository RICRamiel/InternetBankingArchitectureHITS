package org.ricramiel.apigateway.filters;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.headers.CustomHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TokenValidationFilter extends AbstractGatewayFilterFactory<TokenValidationFilter.Config> {

    private final WebClient webClient;

    public TokenValidationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClient = webClientBuilder.build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (exchange.getRequest().getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return chain.filter(exchange);
            }

            String token = authHeader.substring(7);

            String traceId = exchange.getRequest().getHeaders().getFirst(CustomHeaders.CORRELATION_ID_HEADER);
            String spanId = exchange.getRequest().getHeaders().getFirst(CustomHeaders.SPAN_ID_HEADER);

            return webClient.get()
                    .uri(config.getTokenValidationEndpointUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .headers(headers -> {
                        if (traceId != null) {
                            headers.set(CustomHeaders.CORRELATION_ID_HEADER, traceId);
                        }
                        if (spanId != null) {
                            headers.set(CustomHeaders.SPAN_ID_HEADER, spanId);
                        }
                    })
                    .retrieve()
                    .toBodilessEntity()
                    .flatMap(response -> chain.filter(exchange))
                    .onErrorResume(e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    @Setter
    @Getter
    public static class Config {
        private String tokenValidationEndpointUrl;
    }
}
