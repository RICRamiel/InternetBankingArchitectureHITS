package org.ricramiel.common.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.ricramiel.common.headers.CustomHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdempotencyFilterTest {

    private final InMemoryStore store = new InMemoryStore();
    private final IdempotencyFilter<TestRecord> filter = new IdempotencyFilter<>(store);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsMutatingRequestWithoutHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/core-api/transactions/transfer");
        request.setContentType("application/json");
        request.setContent("{\"amount\":100}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, noopChain());

        assertEquals(400, response.getStatus());
        assertEquals("{\"errors\":[\"Missing Idempotency-Key header for mutating request\"]}", response.getContentAsString());
    }

    @Test
    void replaysCompletedResponseForSameKeyAndSameRequest() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-1", null)
        );

        MockHttpServletRequest firstRequest = new MockHttpServletRequest("POST", "/api/core-api/transactions/transfer");
        firstRequest.setContentType("application/json");
        firstRequest.setContent("{\"amount\":100}".getBytes(StandardCharsets.UTF_8));
        firstRequest.addHeader(CustomHeaders.IDEMPOTENCY_KEY_HEADER, "abc-123");

        MockHttpServletResponse firstResponse = new MockHttpServletResponse();

        filter.doFilter(firstRequest, firstResponse, (request, response) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(201);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"status\":\"ok\"}");
        });

        MockHttpServletRequest duplicateRequest = new MockHttpServletRequest("POST", "/api/core-api/transactions/transfer");
        duplicateRequest.setContentType("application/json");
        duplicateRequest.setContent("{\"amount\":100}".getBytes(StandardCharsets.UTF_8));
        duplicateRequest.addHeader(CustomHeaders.IDEMPOTENCY_KEY_HEADER, "abc-123");

        MockHttpServletResponse duplicateResponse = new MockHttpServletResponse();

        filter.doFilter(duplicateRequest, duplicateResponse, (request, response) -> {
            throw new AssertionError("Business logic must not execute for duplicate request");
        });

        assertEquals(201, duplicateResponse.getStatus());
        assertEquals("application/json", duplicateResponse.getContentType());
        assertEquals("{\"status\":\"ok\"}", duplicateResponse.getContentAsString());
    }

    @Test
    void rejectsReusedKeyForDifferentPayload() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-1", null)
        );

        MockHttpServletRequest firstRequest = new MockHttpServletRequest("POST", "/api/core-api/transactions/transfer");
        firstRequest.setContentType("application/json");
        firstRequest.setContent("{\"amount\":100}".getBytes(StandardCharsets.UTF_8));
        firstRequest.addHeader(CustomHeaders.IDEMPOTENCY_KEY_HEADER, "same-key");

        MockHttpServletResponse firstResponse = new MockHttpServletResponse();

        filter.doFilter(firstRequest, firstResponse, (request, response) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(200);
            httpResponse.getWriter().write("{\"status\":\"ok\"}");
        });

        MockHttpServletRequest secondRequest = new MockHttpServletRequest("POST", "/api/core-api/transactions/transfer");
        secondRequest.setContentType("application/json");
        secondRequest.setContent("{\"amount\":200}".getBytes(StandardCharsets.UTF_8));
        secondRequest.addHeader(CustomHeaders.IDEMPOTENCY_KEY_HEADER, "same-key");

        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        filter.doFilter(secondRequest, secondResponse, noopChain());

        assertEquals(409, secondResponse.getStatus());
        assertEquals("{\"errors\":[\"Idempotency-Key cannot be reused with a different request\"]}", secondResponse.getContentAsString());
    }

    @Test
    void removesInProgressRecordWhenHandlerFails() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-1", null)
        );

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/core-api/transactions/transfer");
        request.setContentType("application/json");
        request.setContent("{\"amount\":100}".getBytes(StandardCharsets.UTF_8));
        request.addHeader(CustomHeaders.IDEMPOTENCY_KEY_HEADER, "retry-key");

        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            filter.doFilter(request, response, (req, res) -> {
                throw new RuntimeException("boom");
            });
        } catch (RuntimeException ignored) {
            // expected
        }

        assertEquals(Optional.empty(), store.find("retry-key", "user-1"));
    }

    private FilterChain noopChain() {
        return (request, response) -> { };
    }

    private static final class InMemoryStore implements HttpIdempotencyStore<TestRecord> {
        private final Map<String, TestRecord> records = new ConcurrentHashMap<>();

        @Override
        public Optional<TestRecord> find(String idempotencyKey, String userScope) {
            return Optional.ofNullable(records.get(composeKey(idempotencyKey, userScope)));
        }

        @Override
        public TestRecord createInProgress(String idempotencyKey, String userScope, String requestFingerprint) {
            TestRecord record = new TestRecord();
            record.idempotencyKey = idempotencyKey;
            record.userScope = userScope;
            record.requestFingerprint = requestFingerprint;
            record.status = IdempotencyStatus.IN_PROGRESS;
            return record;
        }

        @Override
        public TestRecord save(TestRecord record) {
            records.put(composeKey(record.idempotencyKey, record.userScope), record.copy());
            return record;
        }

        @Override
        public void delete(TestRecord record) {
            records.remove(composeKey(record.idempotencyKey, record.userScope));
        }

        private String composeKey(String idempotencyKey, String userScope) {
            return userScope + "::" + idempotencyKey;
        }
    }

    private static final class TestRecord implements HttpIdempotencyRecord {
        private String idempotencyKey;
        private String userScope;
        private String requestFingerprint;
        private IdempotencyStatus status;
        private Integer responseStatus;
        private byte[] responseBody;
        private String responseContentType;

        @Override
        public String getIdempotencyKey() {
            return idempotencyKey;
        }

        @Override
        public String getUserScope() {
            return userScope;
        }

        @Override
        public String getRequestFingerprint() {
            return requestFingerprint;
        }

        @Override
        public IdempotencyStatus getStatus() {
            return status;
        }

        @Override
        public Integer getResponseStatus() {
            return responseStatus;
        }

        @Override
        public byte[] getResponseBody() {
            return responseBody;
        }

        @Override
        public String getResponseContentType() {
            return responseContentType;
        }

        @Override
        public void setStatus(IdempotencyStatus status) {
            this.status = status;
        }

        @Override
        public void setResponseStatus(Integer responseStatus) {
            this.responseStatus = responseStatus;
        }

        @Override
        public void setResponseBody(byte[] responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public void setResponseContentType(String responseContentType) {
            this.responseContentType = responseContentType;
        }

        private TestRecord copy() {
            TestRecord copy = new TestRecord();
            copy.idempotencyKey = idempotencyKey;
            copy.userScope = userScope;
            copy.requestFingerprint = requestFingerprint;
            copy.status = status;
            copy.responseStatus = responseStatus;
            copy.responseContentType = responseContentType;
            copy.responseBody = responseBody == null ? null : responseBody.clone();
            return copy;
        }
    }
}
