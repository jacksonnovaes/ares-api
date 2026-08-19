"use client";

import { AuthProvider } from "@/contexts/auth-context";
import { BrandProvider } from "@/contexts/brand-context";
import { AppRouterCacheProvider } from "@mui/material-nextjs/v15-appRouter";

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <AppRouterCacheProvider options={{ enableCssLayer: true }}>
      <AuthProvider>
        <BrandProvider>{children}</BrandProvider>
      </AuthProvider>
    </AppRouterCacheProvider>
  );
}
