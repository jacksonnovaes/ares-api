"use client";

import { BrandMark } from "@/components/common/brand-mark";
import { useBrand } from "@/contexts/brand-context";
import AutoAwesomeRoundedIcon from "@mui/icons-material/AutoAwesomeRounded";
import CheckCircleRoundedIcon from "@mui/icons-material/CheckCircleRounded";
import { Box, Container, Stack, Typography } from "@mui/material";

export function AuthShell({ children, title, subtitle }: { children: React.ReactNode; title: string; subtitle: string }) {
  const { brand } = useBrand();
  return (
    <Box sx={{ minHeight: "100vh", display: "grid", gridTemplateColumns: { xs: "1fr", lg: "minmax(520px, 46%) 1fr" }, bgcolor: "white" }}>
      <Box sx={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
        <Container maxWidth="sm" sx={{ px: { xs: 2.5, sm: 5 }, pt: { xs: 3, sm: 4 }, pb: 3 }}>
          <BrandMark />
        </Container>
        <Container maxWidth="sm" sx={{ px: { xs: 2.5, sm: 5 }, flex: 1, display: "flex", alignItems: "center", py: 5 }}>
          <Box sx={{ width: "100%", maxWidth: 470 }}>
            <Typography component="h1" variant="h1" sx={{ fontSize: { xs: "2rem", sm: "2.45rem" } }}>{title}</Typography>
            <Typography color="text.secondary" sx={{ mt: 1.25, mb: 4, fontSize: 16 }}>{subtitle}</Typography>
            {children}
          </Box>
        </Container>
        <Container maxWidth="sm" sx={{ px: { xs: 2.5, sm: 5 }, pb: 3 }}><Typography variant="caption" color="text.secondary">© {new Date().getFullYear()} {brand.tradeName}</Typography></Container>
      </Box>
      <Box sx={{
        display: { xs: "none", lg: "flex" }, position: "relative", overflow: "hidden", color: "white", p: 8,
        alignItems: "center", justifyContent: "center",
        background: `linear-gradient(145deg, #101828 0%, ${brand.primaryColor} 70%, ${brand.secondaryColor} 140%)`,
        "&::before": { content: '""', position: "absolute", width: 520, height: 520, borderRadius: "50%", right: -160, top: -180, border: "90px solid rgba(255,255,255,.06)" },
        "&::after": { content: '""', position: "absolute", width: 320, height: 320, borderRadius: 12, left: -170, bottom: -170, transform: "rotate(24deg)", bgcolor: "rgba(255,255,255,.05)" },
      }}>
        <Box sx={{ position: "relative", zIndex: 1, maxWidth: 560 }}>
          <Box sx={{ display: "inline-flex", alignItems: "center", gap: 1, px: 1.5, py: 0.75, borderRadius: 99, bgcolor: "rgba(255,255,255,.12)", border: "1px solid rgba(255,255,255,.14)", mb: 3 }}>
            <AutoAwesomeRoundedIcon fontSize="small" /><Typography variant="body2" fontWeight={700}>Operação em um só lugar</Typography>
          </Box>
          <Typography sx={{ fontSize: { lg: "2.65rem", xl: "3.25rem" }, lineHeight: 1.1, letterSpacing: "-.045em", fontWeight: 790 }}>
            Atendimento ágil, serviço organizado e clientes mais felizes.
          </Typography>
          <Typography sx={{ mt: 2.5, mb: 4, color: "rgba(255,255,255,.72)", fontSize: 17, maxWidth: 500 }}>
            Da entrada do cliente à conclusão da ordem de serviço, acompanhe tudo com clareza.
          </Typography>
          <Stack spacing={1.5}>
            {["Clientes e ativos conectados", "Catálogo e valores padronizados", "Ordens com status em tempo real"].map((item) => (
              <Stack key={item} direction="row" alignItems="center" spacing={1.25}><CheckCircleRoundedIcon sx={{ color: "rgba(255,255,255,.85)" }} /><Typography>{item}</Typography></Stack>
            ))}
          </Stack>
        </Box>
      </Box>
    </Box>
  );
}
