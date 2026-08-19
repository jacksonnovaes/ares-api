"use client";

import { BrandMark } from "@/components/common/brand-mark";
import { PageLoading } from "@/components/common/feedback";
import { useAuth } from "@/contexts/auth-context";
import { initials } from "@/lib/format";
import type { Permission } from "@/lib/types";
import BuildCircleOutlinedIcon from "@mui/icons-material/BuildCircleOutlined";
import DashboardRoundedIcon from "@mui/icons-material/DashboardRounded";
import DesignServicesOutlinedIcon from "@mui/icons-material/DesignServicesOutlined";
import DevicesOtherRoundedIcon from "@mui/icons-material/DevicesOtherRounded";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import MenuRoundedIcon from "@mui/icons-material/MenuRounded";
import PaletteOutlinedIcon from "@mui/icons-material/PaletteOutlined";
import PeopleAltOutlinedIcon from "@mui/icons-material/PeopleAltOutlined";
import PersonOutlineRoundedIcon from "@mui/icons-material/PersonOutlineRounded";
import ShieldOutlinedIcon from "@mui/icons-material/ShieldOutlined";
import {
  AppBar, Avatar, Box, Divider, Drawer, IconButton, List, ListItemButton, ListItemIcon,
  ListItemText, Menu, MenuItem, Toolbar, Tooltip, Typography,
} from "@mui/material";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";

const drawerWidth = 270;
const items: { label: string; href: string; icon: React.ReactNode; permission?: Permission }[] = [
  { label: "Visão geral", href: "/dashboard", icon: <DashboardRoundedIcon /> },
  { label: "Clientes", href: "/clientes", icon: <PeopleAltOutlinedIcon />, permission: "CUSTOMER_READ" },
  { label: "Ativos", href: "/ativos", icon: <DevicesOtherRoundedIcon />, permission: "ASSET_READ" },
  { label: "Catálogo", href: "/servicos", icon: <DesignServicesOutlinedIcon />, permission: "SERVICE_READ" },
  { label: "Ordens de serviço", href: "/ordens", icon: <BuildCircleOutlinedIcon />, permission: "SERVICE_ORDER_READ" },
  { label: "Usuários", href: "/usuarios", icon: <PersonOutlineRoundedIcon />, permission: "USER_MANAGE" },
  { label: "Aparência", href: "/aparencia", icon: <PaletteOutlinedIcon /> },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const { user, loading, can, logout } = useAuth();
  const pathname = usePathname();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchor, setAnchor] = useState<null | HTMLElement>(null);

  if (loading || !user) return <PageLoading label="Preparando seu ambiente..." />;

  const navigation = (
    <Box sx={{ display: "flex", flexDirection: "column", height: "100%", bgcolor: "#101828", color: "white" }}>
      <Box sx={{ px: 2.5, height: 78, display: "flex", alignItems: "center" }}><BrandMark inverse /></Box>
      <Divider sx={{ borderColor: "rgba(255,255,255,.08)" }} />
      <List sx={{ px: 1.5, pt: 2, flex: 1 }}>
        {items.filter((item) => !item.permission || can(item.permission)).map((item) => {
          const selected = pathname === item.href || pathname.startsWith(`${item.href}/`);
          return (
            <ListItemButton
              component={Link}
              href={item.href}
              key={item.href}
              selected={selected}
              onClick={() => setMobileOpen(false)}
              sx={{
                mb: 0.5, minHeight: 46, borderRadius: 2.25, color: selected ? "white" : "rgba(255,255,255,.68)",
                "&.Mui-selected": { bgcolor: "primary.main", "&:hover": { bgcolor: "primary.dark" } },
                "&:hover": { bgcolor: "rgba(255,255,255,.07)", color: "white" },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40, color: "inherit" }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} primaryTypographyProps={{ fontSize: 14, fontWeight: selected ? 750 : 560 }} />
            </ListItemButton>
          );
        })}
      </List>
      <Box sx={{ p: 2 }}>
        <Box sx={{ p: 1.5, border: "1px solid rgba(255,255,255,.1)", borderRadius: 2.5, bgcolor: "rgba(255,255,255,.04)" }}>
          <Typography variant="caption" sx={{ color: "rgba(255,255,255,.55)" }}>Organização</Typography>
          <Typography fontSize={13.5} fontWeight={700} noWrap>{user.tenant.name}</Typography>
        </Box>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: "flex", minHeight: "100vh" }}>
      <Drawer variant="permanent" sx={{ display: { xs: "none", lg: "block" }, width: drawerWidth, "& .MuiDrawer-paper": { width: drawerWidth, border: 0 } }}>{navigation}</Drawer>
      <Drawer open={mobileOpen} onClose={() => setMobileOpen(false)} sx={{ display: { lg: "none" }, "& .MuiDrawer-paper": { width: drawerWidth, border: 0 } }}>{navigation}</Drawer>
      <Box sx={{ flex: 1, minWidth: 0, ml: { lg: `${drawerWidth}px` } }}>
        <AppBar position="sticky" color="inherit" elevation={0} sx={{ borderBottom: "1px solid", borderColor: "divider", bgcolor: "rgba(255,255,255,.92)", backdropFilter: "blur(14px)" }}>
          <Toolbar sx={{ minHeight: { xs: 64, sm: 72 }, px: { xs: 2, sm: 3.5 } }}>
            <IconButton onClick={() => setMobileOpen(true)} sx={{ display: { lg: "none" }, mr: 1 }} aria-label="Abrir menu"><MenuRoundedIcon /></IconButton>
            <Box sx={{ flex: 1 }}>
              <Typography variant="body2" color="text.secondary">Olá, {user.name.split(" ")[0]}</Typography>
              <Typography fontWeight={750}>Como estão as operações hoje?</Typography>
            </Box>
            <Tooltip title="Menu da conta">
              <IconButton onClick={(event) => setAnchor(event.currentTarget)} aria-label="Menu da conta">
                <Avatar sx={{ width: 38, height: 38, bgcolor: "primary.main", fontSize: 14, fontWeight: 800 }}>{initials(user.name)}</Avatar>
              </IconButton>
            </Tooltip>
            <Menu anchorEl={anchor} open={Boolean(anchor)} onClose={() => setAnchor(null)} slotProps={{ paper: { sx: { mt: 1, minWidth: 230 } } }}>
              <Box sx={{ px: 2, py: 1 }}><Typography fontWeight={700}>{user.name}</Typography><Typography variant="body2" color="text.secondary">{user.email}</Typography></Box>
              <Divider />
              <MenuItem component={Link} href="/aparencia"><ListItemIcon><PaletteOutlinedIcon fontSize="small" /></ListItemIcon>Aparência</MenuItem>
              <MenuItem component={Link} href="/seguranca"><ListItemIcon><ShieldOutlinedIcon fontSize="small" /></ListItemIcon>Segurança</MenuItem>
              <MenuItem onClick={logout}><ListItemIcon><LogoutRoundedIcon fontSize="small" /></ListItemIcon>Sair</MenuItem>
            </Menu>
          </Toolbar>
        </AppBar>
        <Box component="main" sx={{ p: { xs: 2, sm: 3.5, xl: 4.5 }, maxWidth: 1560, mx: "auto" }}>{children}</Box>
      </Box>
    </Box>
  );
}
