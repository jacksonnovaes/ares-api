"use client";

import { useBrand } from "@/contexts/brand-context";
import { Box, Typography } from "@mui/material";

export function BrandMark({ compact = false, inverse = false }: { compact?: boolean; inverse?: boolean }) {
  const { brand } = useBrand();
  return (
    <Box sx={{ display: "flex", alignItems: "center", gap: 1.25, minWidth: 0 }}>
      {brand.logoUrl ? (
        <Box
          component="img"
          src={brand.logoUrl}
          alt={`Logo ${brand.tradeName}`}
          sx={{ width: 38, height: 38, objectFit: "contain", borderRadius: 1.5, bgcolor: inverse ? "white" : "transparent" }}
        />
      ) : (
        <Box sx={{
          width: 38,
          height: 38,
          borderRadius: 2,
          display: "grid",
          placeItems: "center",
          color: "white",
          fontWeight: 900,
          fontSize: 18,
          background: `linear-gradient(135deg, ${brand.primaryColor}, ${brand.secondaryColor})`,
          boxShadow: `0 8px 20px ${brand.primaryColor}35`,
        }}>
          {brand.tradeName.trim().charAt(0).toUpperCase() || "A"}
        </Box>
      )}
      {!compact && (
        <Box sx={{ minWidth: 0 }}>
          <Typography sx={{ color: inverse ? "white" : "text.primary", fontWeight: 800, lineHeight: 1.1 }} noWrap>
            {brand.tradeName}
          </Typography>
          <Typography variant="caption" sx={{ color: inverse ? "rgba(255,255,255,.62)" : "text.secondary" }}>
            Gestão de serviços
          </Typography>
        </Box>
      )}
    </Box>
  );
}
