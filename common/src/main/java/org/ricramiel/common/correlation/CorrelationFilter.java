package org.ricramiel.common.correlation;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ricramiel.common.headers.CustomHeaders;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

import static org.ricramiel.common.correlation.CorrelationConstants.CORRELATION_ID_MDC;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String correlationId = getCorrelationId((HttpServletRequest) request);
            MDC.put(CORRELATION_ID_MDC, correlationId);
            ((HttpServletResponse) response).addHeader(CustomHeaders.CORRELATION_ID_HEADER, correlationId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_MDC);
        }
    }

    private String getCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CustomHeaders.CORRELATION_ID_HEADER);
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}