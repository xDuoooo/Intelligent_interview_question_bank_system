"use client";

import { createContext, useContext } from "react";

export const AuthInitContext = createContext(false);

export function useAuthInitialized() {
  return useContext(AuthInitContext);
}
