package org.ricramiel.apigateway.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.dtos.EventMetricDto;
import org.ricramiel.common.headers.CustomHeaders;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MetricsFilter implements WebFilter {

    @SuppressWarnings("rawtypes")
    private final KafkaTemplate kafkaTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        String uri = request.getURI().getPath();
        String[] parts = uri.split("/");
        String routedService = parts.length >= 3 ? parts[2] : "unknown";

        String traceId = request.getHeaders().getFirst(CustomHeaders.CORRELATION_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        String parentSpanId = request.getHeaders().getFirst(CustomHeaders.SPAN_ID_HEADER);
        String spanId = UUID.randomUUID().toString();
        String finalTraceId = traceId;
        String finalSpanId = spanId;

        ServerWebExchange tracedExchange = exchange.mutate()
                .request(builder -> builder.headers(headers -> {
                    headers.set(CustomHeaders.CORRELATION_ID_HEADER, finalTraceId);
                    headers.set(CustomHeaders.SPAN_ID_HEADER, finalSpanId);
                }))
                .build();
        tracedExchange.getResponse().getHeaders().set(CustomHeaders.CORRELATION_ID_HEADER, finalTraceId);
        tracedExchange.getResponse().getHeaders().set(CustomHeaders.SPAN_ID_HEADER, finalSpanId);

        log.info("[MetricsFilter WebFlux] Request intercepted: {}, routedService={}, traceId={}",
                uri, routedService, finalTraceId);

        String finalParentSpanId = parentSpanId;
        return chain.filter(tracedExchange).doFinally(signalType -> {
            long duration = System.currentTimeMillis() - startTime;
            int status = tracedExchange.getResponse().getStatusCode() != null
                    ? tracedExchange.getResponse().getStatusCode().value()
                    : 500;

            EventMetricDto metric = EventMetricDto.builder()
                    .time(LocalDateTime.now())
                    .traceId(finalTraceId)
                    .spanId(finalSpanId)
                    .parentSpanId(finalParentSpanId)
                    .serviceName("api-gateway")
                    .operationType("GATEWAY")
                    .method(request.getMethod().name())
                    .endpoint(uri)
                    .durationMs((int) duration)
                    .statusCode(status)
                    .isError(status >= 400)
                    .build();

            log.info("[MetricsFilter WebFlux] Sending metric: status={}, duration={}ms", status, duration);
            kafkaTemplate.send("metrics-topic", finalTraceId, metric);
        });
    }
}
