"use client";

import { useAuth } from "@/contexts/auth-context";
import { apiRequest } from "@/lib/api";
import type { Branding } from "@/lib/types";
import { createTheme, CssBaseline, ThemeProvider } from "@mui/material";
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

export interface BrandSettings {
  tradeName: string;
  logoUrl: string;
  primaryColor: string;
  secondaryColor: string;
  borderRadius: number;
}

const defaults: BrandSettings = {
  tradeName: "Ares",
  logoUrl: "",
  primaryColor: "#2457E6",
  secondaryColor: "#16A085",
  borderRadius: 8,
};

interface BrandContextValue {
  brand: BrandSettings;
  remoteBrand: Branding | null;
  saveBrand: (settings: BrandSettings) => void;
  restoreRemoteBrand: () => void;
  loadBranding: (slug: string) => Promise<void>;
}

const BrandContext = createContext<BrandContextValue | null>(null);

function storageKey(slug?: string) {
  return `ares.brand.${slug || "default"}`;
}

function fromRemote(remote: Branding): BrandSettings {
  return {
    ...defaults,
    tradeName: remote.tradeName || defaults.tradeName,
    logoUrl: remote.logoUrl || "",
    primaryColor: remote.primaryColor || defaults.primaryColor,
  };
}

export function BrandProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  const [brand, setBrand] = useState(defaults);
  const [remoteBrand, setRemoteBrand] = useState<Branding | null>(null);

  const applyStored = useCallback((base: BrandSettings, slug?: string) => {
    try {
      const stored = localStorage.getItem(storageKey(slug));
      setBrand(stored ? { ...base, ...JSON.parse(stored) } : base);
    } catch {
      setBrand(base);
    }
  }, []);

  const loadBranding = useCallback(async (slug: string) => {
    const remote = await apiRequest<Branding>(`/branding?slug=${encodeURIComponent(slug)}`);
    setRemoteBrand(remote);
    localStorage.setItem("ares.lastTenantSlug", slug);
    applyStored(fromRemote(remote), slug);
  }, [applyStored]);

  useEffect(() => {
    const querySlug = new URLSearchParams(window.location.search).get("tenant");
    const slug = user?.tenant.slug || querySlug || localStorage.getItem("ares.lastTenantSlug");
    if (slug) loadBranding(slug).catch(() => applyStored(defaults, slug));
    else applyStored(defaults);
  }, [applyStored, loadBranding, user?.tenant.slug]);

  const saveBrand = useCallback((settings: BrandSettings) => {
    setBrand(settings);
    localStorage.setItem(storageKey(user?.tenant.slug), JSON.stringify(settings));
  }, [user?.tenant.slug]);

  const restoreRemoteBrand = useCallback(() => {
    const restored = remoteBrand ? fromRemote(remoteBrand) : defaults;
    localStorage.removeItem(storageKey(user?.tenant.slug));
    setBrand(restored);
  }, [remoteBrand, user?.tenant.slug]);

  const theme = useMemo(() => createTheme({
    palette: {
      mode: "light",
      primary: { main: brand.primaryColor },
      secondary: { main: brand.secondaryColor },
      background: { default: "#F5F7FB", paper: "#FFFFFF" },
      text: { primary: "#172033", secondary: "#667085" },
      divider: "#E5E9F2",
    },
    shape: { borderRadius: brand.borderRadius },
    typography: {
      fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, sans-serif",
      h1: { fontSize: "2rem", fontWeight: 760, letterSpacing: "-0.035em" },
      h2: { fontSize: "1.5rem", fontWeight: 740, letterSpacing: "-0.025em" },
      h3: { fontSize: "1.125rem", fontWeight: 700 },
      button: { textTransform: "none", fontWeight: 700 },
    },
    components: {
      MuiButton: {
        defaultProps: { disableElevation: true },
        styleOverrides: { root: { minHeight: 42, paddingInline: 18 } },
      },
      MuiCard: {
        styleOverrides: { root: { border: "1px solid #E5E9F2", boxShadow: "0 1px 3px rgba(16,24,40,.04)" } },
      },
      MuiTextField: { defaultProps: { size: "small" } },
      MuiFormControl: { defaultProps: { size: "small" } },
      MuiDialog: { styleOverrides: { paper: { backgroundImage: "none" } } },
    },
  }), [brand]);

  const value = useMemo(() => ({ brand, remoteBrand, saveBrand, restoreRemoteBrand, loadBranding }),
    [brand, loadBranding, remoteBrand, restoreRemoteBrand, saveBrand]);

  return (
    <BrandContext.Provider value={value}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </BrandContext.Provider>
  );
}

export function useBrand() {
  const context = useContext(BrandContext);
  if (!context) throw new Error("useBrand deve ser usado dentro de BrandProvider");
  return context;
}
