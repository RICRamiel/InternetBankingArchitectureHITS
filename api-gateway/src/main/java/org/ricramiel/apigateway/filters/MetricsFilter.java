package org.ricramiel.apigateway.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventMetricDto;
import org.ricramiel.common.headers.CustomHeaders;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MetricsFilter implements WebFilter { // <-- ИМЕННО WebFilter, а не OncePerRequestFilter

    private final KafkaTemplate<String, EventMetricDto> kafkaTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();

        String uri = exchange.getRequest().getURI().getPath();
        String[] parts = uri.split("/");
        String serviceName = parts.length >= 3 ? parts[2] : "unknown";

        // В WebFlux заголовки берутся так
        String traceId = exchange.getRequest().getHeaders().getFirst(CustomHeaders.CORRELATION_ID_HEADER);

        log.info("[MetricsFilter WebFlux] Перехвачен запрос: {}, ServiceName: {}", uri, serviceName);

        // Запускаем цепочку и ловим момент завершения (успешного или с ошибкой)
        return chain.filter(exchange).doFinally(signalType -> {

            long duration = System.currentTimeMillis() - startTime;

            // В WebFlux статус берется из объекта ответа
            int status = exchange.getResponse().getStatusCode() != null ?
                    exchange.getResponse().getStatusCode().value() : 500;

            EventMetricDto metric = EventMetricDto.builder()
                    .time(LocalDateTime.now())
                    .traceId(traceId)
                    .serviceName(serviceName)
                    .endpoint(uri)
                    .durationMs((int) duration)
                    .statusCode(status)
                    .isError(status >= 400)
                    .build();

            log.info("[MetricsFilter WebFlux] Отправляю метрику: Status={}, Duration={}ms", status, duration);
            kafkaTemplate.send("metrics-topic", metric);
        });
    }
}