package org.ricramiel.common.tracing;

import org.ricramiel.common.dtos.EventAccountCreate;
import org.ricramiel.common.dtos.EventTransactionDto;

import java.util.UUID;

public final class TraceHeaders {
    private TraceHeaders() {
    }

    public static void applyCurrentTrace(EventTransactionDto event) {
        TraceContext.TraceState state = TraceContext.current();
        event.setTraceId(state == null ? UUID.randomUUID().toString() : state.traceId());
        event.setParentSpanId(state == null ? null : state.spanId());
    }

    public static void applyCurrentTrace(EventAccountCreate event) {
        TraceContext.TraceState state = TraceContext.current();
        event.setTraceId(state == null ? UUID.randomUUID().toString() : state.traceId());
        event.setParentSpanId(state == null ? null : state.spanId());
    }

    public static TraceContext.TraceState openFrom(EventTransactionDto event) {
        return TraceContext.openChild(event.getTraceId(), event.getParentSpanId());
    }

    public static TraceContext.TraceState openFrom(EventAccountCreate event) {
        return TraceContext.openChild(event.getTraceId(), event.getParentSpanId());
    }
}
