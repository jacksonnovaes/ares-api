"use client";

import { apiRequest } from "@/lib/api";
import type { AuthenticationResult, Me, Permission } from "@/lib/types";
import { usePathname, useRouter } from "next/navigation";
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

interface AuthContextValue {
  user: Me | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  can: (...permissions: Permission[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);
const publicPaths = ["/", "/cadastro", "/recuperar-senha", "/redefinir-senha"];

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<Me | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();
  const pathname = usePathname();

  const refreshUser = useCallback(async () => {
    try {
      const me = await apiRequest<Me>("/auth/me");
      setUser(me);
    } catch {
      setUser(null);
    }
  }, []);

  useEffect(() => {
    refreshUser().finally(() => setLoading(false));
  }, [refreshUser]);

  useEffect(() => {
    if (loading) return;
    const isPublic = publicPaths.includes(pathname);
    if (!user && !isPublic) router.replace("/");
    if (user && pathname === "/") router.replace("/dashboard");
  }, [loading, pathname, router, user]);

  const login = useCallback(async (email: string, password: string) => {
    await apiRequest<AuthenticationResult>("/auth/login", {
      method: "POST",
      body: { email, password },
    });
    await refreshUser();
    router.replace("/dashboard");
  }, [refreshUser, router]);

  const logout = useCallback(async () => {
    try {
      await apiRequest<void>("/auth/logout", { method: "POST" });
    } finally {
      setUser(null);
      router.replace("/");
    }
  }, [router]);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    loading,
    login,
    logout,
    refreshUser,
    can: (...permissions) => permissions.length === 0 || permissions.some((item) => user?.permissions.includes(item)),
  }), [loading, login, logout, refreshUser, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth deve ser usado dentro de AuthProvider");
  return context;
}
