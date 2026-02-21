package org.ricramiel.apigateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.headers.CustomHeaders;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements GlobalFilter {
    private static final String CORRELATION_ID_MDC = "correlationId";
    private static final String REQUEST_PATH_MDC = "requestPath";
    private static final String METHOD_MDC = "method";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String correlationId = generateOrGetCorrelationId(request);

        MDC.put(CORRELATION_ID_MDC, correlationId);
        MDC.put(REQUEST_PATH_MDC, request.getPath().toString());
        MDC.put(METHOD_MDC, request.getMethod().name());

        log.info("Incoming request: {} {}, Headers: {}",
                request.getMethod(),
                request.getPath(),
                request.getHeaders());

        if (!request.getHeaders().containsKey(CustomHeaders.CORRELATION_ID_HEADER)) {
            exchange = exchange.mutate()
                    .request(builder ->
                            builder.headers(headers ->
                                    headers.add(CustomHeaders.CORRELATION_ID_HEADER, correlationId)
                            )
                    )
                    .build();
        }

        return chain.filter(exchange)
                .doOnSuccess(done -> {
                    log.info("Request completed successfully");
                })
                .doOnError(throwable -> {
                    log.error("Request failed: {}", throwable.getMessage());
                })
                .doFinally(signal -> {
                    cleanupMdc();
                })
                .contextWrite(Context.of(CORRELATION_ID_MDC, correlationId));
    }

    private static String generateOrGetCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst(CustomHeaders.CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private void cleanupMdc() {
        MDC.remove(CORRELATION_ID_MDC);
        MDC.remove(REQUEST_PATH_MDC);
        MDC.remove(METHOD_MDC);
    }
}