package org.ricramiel.apigateway.controller;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class GatewayFallbackController {

    @RequestMapping(path = "/fallback/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> fallback(
            @PathVariable("service") String service,
            ServerWebExchange exchange
    ) {
        Throwable exception = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "UPSTREAM_TEMPORARILY_UNAVAILABLE");
        body.put("message", "Upstream service is temporarily unavailable. Please retry later.");
        body.put("service", service);
        body.put("path", exchange.getRequest().getPath().value());
        if (exception != null) {
            body.put("reason", exception.getClass().getSimpleName());
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
