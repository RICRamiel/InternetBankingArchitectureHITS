import { configureStore } from "@reduxjs/toolkit";
import { generatedPublicApi, initPublicBffApi } from "@fins/api";
import { apiErrorListener } from "./app/apiErrorListener";

initPublicBffApi({
  baseUrl: import.meta.env.VITE_BFF_URL ?? "/api",
  monitoring: {
    enabled: import.meta.env.VITE_FRONTEND_MONITORING_ENABLED !== "false",
    serviceName: "client-frontend",
    captureWindowErrors:
      import.meta.env.VITE_FRONTEND_MONITORING_CAPTURE_ERRORS === "true",
  },
});

export const store = configureStore({
  reducer: {
    [generatedPublicApi.reducerPath]: generatedPublicApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(
      generatedPublicApi.middleware,
      apiErrorListener.middleware,
    ),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
