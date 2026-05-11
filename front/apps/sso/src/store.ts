import { configureStore } from "@reduxjs/toolkit";
import { generatedSsoApi, initSsoBffApi } from "@fins/api/sso";
import { apiErrorListener } from "./app/apiErrorListener";

initSsoBffApi({
  baseUrl: import.meta.env.VITE_BFF_URL ?? "/api",
  monitoring: {
    enabled: import.meta.env.VITE_FRONTEND_MONITORING_ENABLED !== "false",
    serviceName: "sso-frontend",
    captureWindowErrors:
      import.meta.env.VITE_FRONTEND_MONITORING_CAPTURE_ERRORS === "true",
  },
});

export const store = configureStore({
  reducer: {
    [generatedSsoApi.reducerPath]: generatedSsoApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(
      generatedSsoApi.middleware,
      apiErrorListener.middleware,
    ),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
