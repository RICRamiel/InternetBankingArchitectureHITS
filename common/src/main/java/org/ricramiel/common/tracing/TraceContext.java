package org.ricramiel.common.tracing;

import java.util.UUID;

public final class TraceContext {
    private static final ThreadLocal<TraceState> STATE = new ThreadLocal<>();

    private TraceContext() {
    }

    public static TraceState open(String traceId, String spanId, String parentSpanId) {
        TraceState state = new TraceState(
                normalize(traceId),
                normalize(spanId),
                parentSpanId
        );
        STATE.set(state);
        return state;
    }

    public static TraceState openChild(String traceId, String parentSpanId) {
        return open(traceId, UUID.randomUUID().toString(), parentSpanId);
    }

    public static TraceState currentOrNew() {
        TraceState current = STATE.get();
        if (current != null) {
            return current;
        }
        return open(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null);
    }

    public static TraceState current() {
        return STATE.get();
    }

    public static String traceIdOrNew() {
        return currentOrNew().traceId();
    }

    public static String spanIdOrNew() {
        return currentOrNew().spanId();
    }

    public static void clear() {
        STATE.remove();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? UUID.randomUUID().toString() : value;
    }

    public record TraceState(String traceId, String spanId, String parentSpanId) {
    }
}
