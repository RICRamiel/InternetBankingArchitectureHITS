package org.ricramiel.common.tracing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ricramiel.common.headers.CustomHeaders;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class HttpTraceFilter extends OncePerRequestFilter {
    private static final String CORRELATION_ID_MDC = "correlationId";
    private static final String SPAN_ID_MDC = "spanId";

    private final MonitoringEventPublisher monitoringEventPublisher;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String incomingTraceId = request.getHeader(CustomHeaders.CORRELATION_ID_HEADER);
        String parentSpanId = request.getHeader(CustomHeaders.SPAN_ID_HEADER);
        TraceContext.TraceState trace = TraceContext.openChild(incomingTraceId, parentSpanId);

        MDC.put(CORRELATION_ID_MDC, trace.traceId());
        MDC.put(SPAN_ID_MDC, trace.spanId());
        response.setHeader(CustomHeaders.CORRELATION_ID_HEADER, trace.traceId());
        response.setHeader(CustomHeaders.SPAN_ID_HEADER, trace.spanId());

        Throwable error = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus() > 0 ? response.getStatus() : 500;
            boolean isError = error != null || status >= 400;

            monitoringEventPublisher.publish(monitoringEventPublisher.metric(
                    trace,
                    MonitoringEventPublisher.HTTP_SERVER,
                    request.getMethod(),
                    request.getRequestURI(),
                    null,
                    duration,
                    status,
                    isError,
                    error == null ? null : error.getMessage()
            ));

            MDC.remove(CORRELATION_ID_MDC);
            MDC.remove(SPAN_ID_MDC);
            TraceContext.clear();
        }
    }
}
