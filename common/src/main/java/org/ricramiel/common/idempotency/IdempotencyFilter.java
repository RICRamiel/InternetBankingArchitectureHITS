package org.ricramiel.common.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ricramiel.common.headers.CustomHeaders;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class IdempotencyFilter<T extends HttpIdempotencyRecord> extends OncePerRequestFilter {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> EXCLUDED_PREFIXES = Set.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars"
    );

    private final HttpIdempotencyStore<T> store;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!MUTATING_METHODS.contains(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return EXCLUDED_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String idempotencyKey = request.getHeader(CustomHeaders.IDEMPOTENCY_KEY_HEADER);
        if (!StringUtils.hasText(idempotencyKey)) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Missing Idempotency-Key header for mutating request");
            return;
        }

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);

        String userScope = resolveUserScope(cachedRequest);
        String requestFingerprint = buildRequestFingerprint(cachedRequest, userScope);

        Optional<T> existingRecord = store.find(idempotencyKey, userScope);
        if (existingRecord.isPresent()) {
            handleExistingRecord(existingRecord.get(), requestFingerprint, response);
            return;
        }

        T record;
        try {
            record = store.save(store.createInProgress(idempotencyKey, userScope, requestFingerprint));
        } catch (DataIntegrityViolationException ex) {
            Optional<T> concurrentRecord = store.find(idempotencyKey, userScope);
            if (concurrentRecord.isPresent()) {
                handleExistingRecord(concurrentRecord.get(), requestFingerprint, response);
                return;
            }
            throw ex;
        }

        try {
            filterChain.doFilter(cachedRequest, cachedResponse);

            if (cachedResponse.getStatus() >= 500) {
                store.delete(record);
            } else {
                record.setStatus(IdempotencyStatus.COMPLETED);
                record.setResponseStatus(cachedResponse.getStatus());
                record.setResponseContentType(cachedResponse.getContentType());
                record.setResponseBody(cachedResponse.getContentAsByteArray());
                store.save(record);
            }
        } catch (Exception ex) {
            store.delete(record);
            throw ex;
        } finally {
            cachedResponse.copyBodyToResponse();
        }
    }

    private void handleExistingRecord(T record,
                                      String requestFingerprint,
                                      HttpServletResponse response) throws IOException {
        if (!record.getRequestFingerprint().equals(requestFingerprint)) {
            writeError(response, HttpServletResponse.SC_CONFLICT,
                    "Idempotency-Key cannot be reused with a different request");
            return;
        }

        if (record.getStatus() == IdempotencyStatus.IN_PROGRESS) {
            writeError(response, HttpServletResponse.SC_CONFLICT,
                    "Request with this Idempotency-Key is already being processed");
            return;
        }

        replayStoredResponse(record, response);
    }

    private void replayStoredResponse(T record, HttpServletResponse response) throws IOException {
        response.setStatus(record.getResponseStatus());
        if (StringUtils.hasText(record.getResponseContentType())) {
            response.setContentType(record.getResponseContentType());
        }

        byte[] body = record.getResponseBody();
        if (body != null && body.length > 0) {
            response.getOutputStream().write(body);
        }
    }

    private void writeError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"errors":["%s"]}
                """.formatted(message.replace("\"", "\\\"")).trim());
    }

    private String resolveUserScope(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && StringUtils.hasText(authentication.getName())) {
            return authentication.getName();
        }

        String userIdHeader = request.getHeader(CustomHeaders.USER_ID_HEADER);
        if (StringUtils.hasText(userIdHeader)) {
            return userIdHeader;
        }

        return "anonymous";
    }

    private String buildRequestFingerprint(CachedBodyHttpServletRequest request, String userScope) {
        String payload = request.getMethod()
                + "|"
                + request.getRequestURI()
                + "|"
                + defaultString(request.getQueryString())
                + "|"
                + userScope
                + "|"
                + new String(request.getCachedBody(), StandardCharsets.UTF_8);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
