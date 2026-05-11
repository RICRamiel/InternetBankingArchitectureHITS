import type { FetchArgs } from "@reduxjs/toolkit/query";

export type FrontendMonitoringOptions = {
  enabled?: boolean;
  serviceName: string;
  endpoint?: string;
  captureWindowErrors?: boolean;
};

export type FrontendMetric = {
  time?: string;
  traceId?: string;
  spanId?: string;
  parentSpanId?: string | null;
  serviceName?: string;
  operationType: string;
  method?: string;
  endpoint?: string;
  topic?: string | null;
  durationMs?: number;
  statusCode?: number;
  isError?: boolean;
  errorMessage?: string | null;
};

const CORRELATION_ID_HEADER = "X-Correlation-ID";
const SPAN_ID_HEADER = "X-Span-ID";
const DEFAULT_METRICS_PATH = "/monitoring/metrics";

let currentOptions: FrontendMonitoringOptions | null = null;
let windowErrorsRegistered = false;

export { CORRELATION_ID_HEADER, SPAN_ID_HEADER };

type HeaderInput =
  | HeadersInit
  | string[][]
  | Record<string, string | undefined>
  | undefined;

export function configureFrontendMonitoring(
  options: FrontendMonitoringOptions | false | undefined,
  baseUrl: string,
): void {
  if (options === false) {
    currentOptions = null;
    return;
  }
  if (options === undefined || options.enabled === false) {
    currentOptions = null;
    return;
  }
  currentOptions = {
    ...options,
    endpoint: options.endpoint ?? joinUrl(normalizeBaseUrl(baseUrl), DEFAULT_METRICS_PATH),
  };
  if (options.captureWindowErrors === true) {
    registerWindowErrorHandlers();
  }
}

export function frontendMonitoringEnabled(): boolean {
  return currentOptions !== null && currentOptions.enabled !== false;
}

export function createFrontendTrace(): { traceId: string; spanId: string } {
  return {
    traceId: randomId(),
    spanId: randomId(),
  };
}

export function attachTraceHeaders(
  args: string | FetchArgs,
  trace: { traceId: string; spanId: string },
): FetchArgs {
  if (typeof args === "string") {
    return {
      url: args,
      headers: traceHeaders(undefined, trace),
    };
  }
  return {
    ...args,
    headers: traceHeaders(args.headers, trace),
  };
}

export function tracedHeaders(
  existing: HeaderInput,
  trace: { traceId: string; spanId: string },
): Headers {
  return traceHeaders(existing, trace);
}

export function recordFrontendMetric(metric: FrontendMetric): void {
  const options = currentOptions;
  if (options === null || options.enabled === false) {
    return;
  }
  const endpoint = options.endpoint;
  if (!endpoint) {
    return;
  }

  const payload: FrontendMetric = {
    ...metric,
    time: metric.time ?? new Date().toISOString(),
    serviceName: metric.serviceName ?? options.serviceName,
    endpoint: metric.endpoint ? normalizeEndpoint(metric.endpoint) : undefined,
    errorMessage: trimError(metric.errorMessage),
  };
  const body = JSON.stringify(payload);

  try {
    if (typeof navigator !== "undefined" && "sendBeacon" in navigator) {
      const blob = new Blob([body], { type: "application/json" });
      if (navigator.sendBeacon(endpoint, blob)) {
        return;
      }
    }
  } catch {
    // Metrics must never affect user flows.
  }

  try {
    void fetch(endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body,
      credentials: "same-origin",
      keepalive: true,
    }).catch(() => undefined);
  } catch {
    // ignored
  }
}

export function requestEndpoint(args: string | { url?: string }): string {
  if (typeof args === "string") {
    return args;
  }
  return args.url ?? "unknown";
}

export function requestMethod(args: string | { method?: string }): string {
  if (typeof args === "string") {
    return "GET";
  }
  return (args.method ?? "GET").toUpperCase();
}

export function normalizeEndpoint(endpoint: string): string {
  const withoutQuery = endpoint.split("?")[0] ?? endpoint;
  return withoutQuery
    .replace(
      /[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}/gi,
      "{id}",
    )
    .replace(/\/\d+(?=\/|$)/g, "/{id}");
}

function registerWindowErrorHandlers(): void {
  if (windowErrorsRegistered || typeof window === "undefined") {
    return;
  }
  windowErrorsRegistered = true;

  window.addEventListener("error", (event) => {
    const trace = createFrontendTrace();
    recordFrontendMetric({
      traceId: trace.traceId,
      spanId: trace.spanId,
      operationType: "FRONTEND_ERROR",
      endpoint: window.location.pathname,
      statusCode: 0,
      isError: true,
      errorMessage: event.message || String(event.error ?? "window error"),
    });
  });

  window.addEventListener("unhandledrejection", (event) => {
    const trace = createFrontendTrace();
    recordFrontendMetric({
      traceId: trace.traceId,
      spanId: trace.spanId,
      operationType: "FRONTEND_UNHANDLED_REJECTION",
      endpoint: window.location.pathname,
      statusCode: 0,
      isError: true,
      errorMessage: reasonToString(event.reason),
    });
  });
}

function traceHeaders(
  existing: HeaderInput,
  trace: { traceId: string; spanId: string },
): Headers {
  const headers = toHeaders(existing);
  if (!headers.has(CORRELATION_ID_HEADER)) {
    headers.set(CORRELATION_ID_HEADER, trace.traceId);
  }
  if (!headers.has(SPAN_ID_HEADER)) {
    headers.set(SPAN_ID_HEADER, trace.spanId);
  }
  return headers;
}

function toHeaders(existing: HeaderInput): Headers {
  const headers = new Headers();
  if (existing === undefined) {
    return headers;
  }
  if (existing instanceof Headers) {
    existing.forEach((value, key) => headers.set(key, value));
    return headers;
  }
  if (Array.isArray(existing)) {
    for (const item of existing) {
      if (item.length >= 2) {
        headers.set(item[0], item[1]);
      }
    }
    return headers;
  }
  for (const [key, value] of Object.entries(existing)) {
    if (value !== undefined) {
      headers.set(key, value);
    }
  }
  return headers;
}

function normalizeBaseUrl(baseUrl: string): string {
  return baseUrl.trim().replace(/\/+$/, "");
}

function joinUrl(baseUrl: string, path: string): string {
  const prefix = baseUrl || "";
  const p = path.startsWith("/") ? path : `/${path}`;
  return `${prefix}${p}`;
}

function randomId(): string {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`;
}

function trimError(value: string | null | undefined): string | null | undefined {
  if (value === undefined || value === null || value.length <= 500) {
    return value;
  }
  return value.slice(0, 500);
}

function reasonToString(reason: unknown): string {
  if (reason instanceof Error) {
    return reason.message;
  }
  if (typeof reason === "string") {
    return reason;
  }
  try {
    return JSON.stringify(reason);
  } catch {
    return String(reason);
  }
}
