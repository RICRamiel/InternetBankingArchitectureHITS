package org.ricramiel.userservice.configurations;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ricramiel.common.util.ChaosUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@Order(1)
public class ChaosFilter implements Filter {
    private static final Set<String> SWAGGER_PREFIXES = Set.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars"
    );

    @Value("${chaos.http.enabled:false}")
    private boolean enabled;

    @Value("${chaos.http.even-minute-threshold:70}")
    private int evenMinuteThreshold;

    @Value("${chaos.http.odd-minute-threshold:30}")
    private int oddMinuteThreshold;

    @Value("${chaos.http.status:500}")
    private int status;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (isSwaggerRequest(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        if (enabled && ChaosUtil.shouldSimulateError(evenMinuteThreshold, oddMinuteThreshold)) {
            httpResponse.sendError(status, "Simulated Chaos Error");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isSwaggerRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return SWAGGER_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
