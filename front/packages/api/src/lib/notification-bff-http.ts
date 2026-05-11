import type { Notification } from "../entities/notification";
import {
  type BffCircuitBreaker,
  getSharedBffCircuitBreaker,
} from "./bff-circuit-breaker";
import {
  createFrontendTrace,
  recordFrontendMetric,
  tracedHeaders,
} from "./frontend-monitoring";

export function defaultNotificationsBffBaseUrl(): string {
  return "/api/notifications";
}

function joinUrl(baseUrl: string, path: string): string {
  const b = baseUrl.replace(/\/$/, "");
  const p = path.startsWith("/") ? path : `/${path}`;
  return `${b}${p}`;
}

function recordNotificationResponse(
  breaker: BffCircuitBreaker | null,
  r: Response,
): void {
  if (!breaker) {
    return;
  }
  breaker.recordFromFetchBaseResult({
    error: !r.ok && r.status >= 500 ? { status: r.status } : undefined,
    data: r.ok || r.status < 500 ? {} : undefined,
  });
}

function withTrace(init: RequestInit, trace: { traceId: string; spanId: string }): RequestInit {
  return {
    ...init,
    headers: tracedHeaders(init.headers, trace),
  };
}

function recordNotificationMetric(
  trace: { traceId: string; spanId: string },
  baseUrl: string,
  path: string,
  method: string,
  startedAt: number,
  statusCode: number,
  isError: boolean,
  errorMessage?: string | null,
): void {
  recordFrontendMetric({
    traceId: trace.traceId,
    spanId: trace.spanId,
    operationType: "FRONTEND_HTTP",
    method,
    endpoint: joinUrl(baseUrl, path),
    durationMs: Math.round(performance.now() - startedAt),
    statusCode,
    isError,
    errorMessage,
  });
}

export async function fetchUnreadNotifications(
  baseUrl: string = defaultNotificationsBffBaseUrl(),
  init?: RequestInit,
): Promise<Notification[]> {
  const breaker = getSharedBffCircuitBreaker();
  const trace = createFrontendTrace();
  const startedAt = performance.now();
  if (breaker?.shouldBlock()) {
    recordNotificationMetric(trace, baseUrl, "/unread", "GET", startedAt, 503, true, "BFF_CIRCUIT_OPEN");
    throw new Error("BFF_CIRCUIT_OPEN");
  }
  let r: Response;
  try {
    r = await fetch(joinUrl(baseUrl, "/unread"), withTrace({
      credentials: "include",
      ...init,
    }, trace));
  } catch (err) {
    breaker?.recordFromFetchBaseResult({
      error: { status: "FETCH_ERROR", error: String(err) },
    });
    recordNotificationMetric(trace, baseUrl, "/unread", "GET", startedAt, 0, true, String(err));
    throw err;
  }
  recordNotificationResponse(breaker, r);
  recordNotificationMetric(trace, baseUrl, "/unread", "GET", startedAt, r.status, !r.ok, r.ok ? null : `HTTP ${r.status}`);
  if (!r.ok) {
    throw new Error(`notifications unread HTTP ${r.status}`);
  }
  return (await r.json()) as Notification[];
}

export async function fetchAllNotifications(
  baseUrl: string = defaultNotificationsBffBaseUrl(),
  init?: RequestInit,
): Promise<Notification[]> {
  const breaker = getSharedBffCircuitBreaker();
  const trace = createFrontendTrace();
  const startedAt = performance.now();
  if (breaker?.shouldBlock()) {
    recordNotificationMetric(trace, baseUrl, "/all", "GET", startedAt, 503, true, "BFF_CIRCUIT_OPEN");
    throw new Error("BFF_CIRCUIT_OPEN");
  }
  let r: Response;
  try {
    r = await fetch(joinUrl(baseUrl, "/all"), withTrace({
      credentials: "include",
      ...init,
    }, trace));
  } catch (err) {
    breaker?.recordFromFetchBaseResult({
      error: { status: "FETCH_ERROR", error: String(err) },
    });
    recordNotificationMetric(trace, baseUrl, "/all", "GET", startedAt, 0, true, String(err));
    throw err;
  }
  recordNotificationResponse(breaker, r);
  recordNotificationMetric(trace, baseUrl, "/all", "GET", startedAt, r.status, !r.ok, r.ok ? null : `HTTP ${r.status}`);
  if (!r.ok) {
    throw new Error(`notifications all HTTP ${r.status}`);
  }
  return (await r.json()) as Notification[];
}

export type FcmPlatform = "WEB_CLIENT" | "WEB_WORKER";

export type FcmTokenRequest = {
  token: string;
  platform?: FcmPlatform;
};

export async function registerFcmToken(
  body: FcmTokenRequest,
  baseUrl: string = defaultNotificationsBffBaseUrl(),
  init?: RequestInit,
): Promise<void> {
  const breaker = getSharedBffCircuitBreaker();
  const trace = createFrontendTrace();
  const startedAt = performance.now();
  if (breaker?.shouldBlock()) {
    recordNotificationMetric(trace, baseUrl, "/fcm/token", "POST", startedAt, 503, true, "BFF_CIRCUIT_OPEN");
    throw new Error("BFF_CIRCUIT_OPEN");
  }
  let r: Response;
  try {
    r = await fetch(joinUrl(baseUrl, "/fcm/token"), withTrace({
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
      signal: init?.signal,
    }, trace));
  } catch (err) {
    breaker?.recordFromFetchBaseResult({
      error: { status: "FETCH_ERROR", error: String(err) },
    });
    recordNotificationMetric(trace, baseUrl, "/fcm/token", "POST", startedAt, 0, true, String(err));
    throw err;
  }
  recordNotificationResponse(breaker, r);
  recordNotificationMetric(trace, baseUrl, "/fcm/token", "POST", startedAt, r.status, !r.ok, r.ok ? null : `HTTP ${r.status}`);
  if (!r.ok) {
    throw new Error(`notifications FCM register HTTP ${r.status}`);
  }
}

export async function unregisterFcmToken(
  fcmToken: string,
  baseUrl: string = defaultNotificationsBffBaseUrl(),
  init?: RequestInit,
): Promise<void> {
  const breaker = getSharedBffCircuitBreaker();
  const trace = createFrontendTrace();
  const startedAt = performance.now();
  if (breaker?.shouldBlock()) {
    recordNotificationMetric(trace, baseUrl, "/fcm/token", "DELETE", startedAt, 503, true, "BFF_CIRCUIT_OPEN");
    throw new Error("BFF_CIRCUIT_OPEN");
  }
  const q = new URLSearchParams({ arg0: fcmToken });
  let r: Response;
  try {
    r = await fetch(`${joinUrl(baseUrl, "/fcm/token")}?${q.toString()}`, withTrace({
      method: "DELETE",
      credentials: "include",
      signal: init?.signal,
    }, trace));
  } catch (err) {
    breaker?.recordFromFetchBaseResult({
      error: { status: "FETCH_ERROR", error: String(err) },
    });
    recordNotificationMetric(trace, baseUrl, "/fcm/token", "DELETE", startedAt, 0, true, String(err));
    throw err;
  }
  recordNotificationResponse(breaker, r);
  recordNotificationMetric(trace, baseUrl, "/fcm/token", "DELETE", startedAt, r.status, !r.ok, r.ok ? null : `HTTP ${r.status}`);
  if (!r.ok) {
    throw new Error(`notifications FCM unregister HTTP ${r.status}`);
  }
}

export async function markNotificationRead(
  notificationId: string,
  baseUrl: string = defaultNotificationsBffBaseUrl(),
  init?: RequestInit,
): Promise<void> {
  const breaker = getSharedBffCircuitBreaker();
  const trace = createFrontendTrace();
  const startedAt = performance.now();
  if (breaker?.shouldBlock()) {
    recordNotificationMetric(trace, baseUrl, "/{id}/read", "PUT", startedAt, 503, true, "BFF_CIRCUIT_OPEN");
    throw new Error("BFF_CIRCUIT_OPEN");
  }
  let r: Response;
  try {
    r = await fetch(joinUrl(baseUrl, `/${notificationId}/read`), withTrace({
      method: "PUT",
      credentials: "include",
      ...init,
    }, trace));
  } catch (err) {
    breaker?.recordFromFetchBaseResult({
      error: { status: "FETCH_ERROR", error: String(err) },
    });
    recordNotificationMetric(trace, baseUrl, "/{id}/read", "PUT", startedAt, 0, true, String(err));
    throw err;
  }
  recordNotificationResponse(breaker, r);
  recordNotificationMetric(trace, baseUrl, `/${notificationId}/read`, "PUT", startedAt, r.status, !r.ok, r.ok ? null : `HTTP ${r.status}`);
  if (!r.ok) {
    throw new Error(`notifications mark read HTTP ${r.status}`);
  }
}
