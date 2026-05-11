import { fetchBaseQuery } from "@reduxjs/toolkit/query";
import type {
  BaseQueryFn,
  FetchArgs,
  FetchBaseQueryError,
} from "@reduxjs/toolkit/query";
import {
  resolveSharedBffCircuitBreaker,
  type BffClientCircuitBreakerOption,
} from "./bff-circuit-breaker";
import { BFF_IDEMPOTENCY_KEY_HEADER } from "./bff-idempotency-header";
import {
  attachTraceHeaders,
  configureFrontendMonitoring,
  createFrontendTrace,
  recordFrontendMetric,
  requestEndpoint,
  requestMethod,
  type FrontendMonitoringOptions,
} from "./frontend-monitoring";

type FetchBaseQueryOptions = NonNullable<Parameters<typeof fetchBaseQuery>[0]>;

export { BFF_IDEMPOTENCY_KEY_HEADER };

export type BffClientOptions = {
  baseUrl: string;

  credentials?: RequestCredentials;
  prepareHeaders?: FetchBaseQueryOptions["prepareHeaders"];
  fetchFn?: FetchBaseQueryOptions["fetchFn"];
  
  circuitBreaker?: BffClientCircuitBreakerOption;
  monitoring?: FrontendMonitoringOptions | false;
};

function normalizeBaseUrl(baseUrl: string): string {
  return baseUrl.trim().replace(/\/+$/, "");
}

export function createBffFetchBaseQuery(
  options: BffClientOptions,
): BaseQueryFn<string | FetchArgs, unknown, FetchBaseQueryError> {
  const {
    baseUrl,
    credentials = "include",
    prepareHeaders,
    fetchFn,
    circuitBreaker: circuitBreakerOpt,
    monitoring,
  } = options;

  configureFrontendMonitoring(monitoring, baseUrl);

  const mergedPrepareHeaders: FetchBaseQueryOptions["prepareHeaders"] = (
    headers,
    api,
  ) => {
    if (prepareHeaders !== undefined) {
      prepareHeaders(headers, api);
    }
    if (!headers.has(BFF_IDEMPOTENCY_KEY_HEADER)) {
      headers.set(BFF_IDEMPOTENCY_KEY_HEADER, crypto.randomUUID());
    }
    return headers;
  };

  const inner = fetchBaseQuery({
    baseUrl: normalizeBaseUrl(baseUrl),
    credentials,
    prepareHeaders: mergedPrepareHeaders,
    ...(fetchFn !== undefined ? { fetchFn } : {}),
  });

  return async (args, api, extraOptions) => {
    const breaker = resolveSharedBffCircuitBreaker(circuitBreakerOpt);
    const trace = createFrontendTrace();
    const tracedArgs = attachTraceHeaders(args, trace);
    const startedAt = performance.now();
    const endpoint = requestEndpoint(args);
    const method = requestMethod(args);

    if (breaker?.shouldBlock()) {
      recordFrontendMetric({
        traceId: trace.traceId,
        spanId: trace.spanId,
        operationType: "FRONTEND_HTTP",
        method,
        endpoint,
        durationMs: Math.round(performance.now() - startedAt),
        statusCode: 503,
        isError: true,
        errorMessage: "BFF_CIRCUIT_OPEN",
      });
      return {
        error: {
          status: "CUSTOM_ERROR",
          error: "Сервис временно недоступен",
          data: {
            code: "BFF_CIRCUIT_OPEN",
            message: "Сервис временно недоступен",
          },
        },
      };
    }
    const result = await inner(tracedArgs, api, extraOptions);
    breaker?.recordFromFetchBaseResult(result);
    const error = "error" in result ? result.error : undefined;
    const status = error?.status ?? 200;
    recordFrontendMetric({
      traceId: trace.traceId,
      spanId: trace.spanId,
      operationType: "FRONTEND_HTTP",
      method,
      endpoint,
      durationMs: Math.round(performance.now() - startedAt),
      statusCode: typeof status === "number" ? status : 0,
      isError: error !== undefined,
      errorMessage: error !== undefined ? String(error.status) : null,
    });
    return result;
  };
}
