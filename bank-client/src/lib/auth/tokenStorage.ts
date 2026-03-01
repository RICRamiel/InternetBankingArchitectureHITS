import type { JwtModelDto } from "../api/generatedApi";

const ACCESS_TOKEN_KEY = "bank_access_token";
const REFRESH_TOKEN_KEY = "bank_refresh_token";

export const getAccessToken = (): string | null => {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(ACCESS_TOKEN_KEY);
};

export const getRefreshToken = (): string | null => {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(REFRESH_TOKEN_KEY);
};

export const setTokens = (jwt: JwtModelDto): void => {
  if (typeof window === "undefined") return;
  
  // console.log(`local_access:\t${localStorage.getItem(ACCESS_TOKEN_KEY)}`)
  // console.log(`local_refresh:\t${localStorage.getItem(REFRESH_TOKEN_KEY)}`)

  // console.log(`jwt_access:\t${jwt.accessToken}`)
  // console.log(`jwt_refresh:\t${jwt.refreshToken}`)

  localStorage.setItem(ACCESS_TOKEN_KEY, jwt.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, jwt.refreshToken);
};

export const clearTokens = (): void => {
  if (typeof window === "undefined") return;
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
};

