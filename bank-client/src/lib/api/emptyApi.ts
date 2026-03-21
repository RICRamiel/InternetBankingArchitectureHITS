import {
  createApi,
  fetchBaseQuery,
  type BaseQueryFn,
  type FetchArgs,
  type FetchBaseQueryError,
} from "@reduxjs/toolkit/query/react";
import {
  getAccessToken,
  getRefreshToken,
  setTokens,
  clearTokens,
} from "../auth/tokenStorage";
import type {
  JwtModelDto,
  RefreshAndRotateApiArg,
} from "./generatedApi";

//TODO: add API_URL from other place (idk where)
const MOCK_API_URL = "http://localhost:4010";
const API_URL = "http://api.thallassianangel.su/api";

const AUTH_ENDPOINTS = [
  'login',
  'register',
  'revoke',
];

const rawBaseQuery = fetchBaseQuery({
  baseUrl: API_URL,
  prepareHeaders: (headers, {endpoint, arg}) => {
    const url = typeof arg === "object" && arg?.url;
    const isAuthRequest = AUTH_ENDPOINTS.includes(endpoint) ||
      (typeof url === 'string' && url.includes('/auth/'));
    console.log(`isAuthRequest ${isAuthRequest}, endpoint ${endpoint}`);
    if (!isAuthRequest) {
      const token = getAccessToken();
      if (token) {
        headers.set("Authorization", `Bearer ${token}`);
      }
    }
    return headers;
  }
});

const baseQueryWithReauth: BaseQueryFn<
  string | FetchArgs,
  unknown,
  FetchBaseQueryError
> = async (args, api, extraOptions) => {
  let result = await rawBaseQuery(args, api, extraOptions);

  if (result.error && result.error.status === 401) {
    const refreshToken = getRefreshToken();

    if (refreshToken) {
      const refreshArgs: FetchArgs = {
        url: "/user-service/auth/refresh",
        method: "POST",
        body: {
          value: refreshToken,
        } as RefreshAndRotateApiArg["tokenRefreshModelDto"],
      };

      const refreshResult = await rawBaseQuery(
        refreshArgs,
        api,
        extraOptions
      );

      if (refreshResult.data) {
        const jwt = refreshResult.data as JwtModelDto;
        setTokens(jwt);
        result = await rawBaseQuery(args, api, extraOptions);
      } else {
        clearTokens();
      }
    } else {
      clearTokens();
    }
  }

  return result;
};

export const emptySplitApi = createApi({
  reducerPath: "api",
  baseQuery: baseQueryWithReauth,
  endpoints: () => ({}),
});