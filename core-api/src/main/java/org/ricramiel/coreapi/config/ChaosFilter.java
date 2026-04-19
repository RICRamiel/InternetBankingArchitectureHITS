package org.ricramiel.coreapi.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
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

    private final Random random = new Random();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (isSwaggerRequest(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        int currentMinute = LocalDateTime.now().getMinute();
        boolean isEvenMinute = currentMinute % 2 == 0;
        int errorThreshold = isEvenMinute ? 70 : 30;

        int randomValue = random.nextInt(100);
        if (randomValue < errorThreshold) {
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Simulated Chaos Error (Threshold: " + errorThreshold + "%)");
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
