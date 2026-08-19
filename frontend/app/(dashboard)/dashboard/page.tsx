"use client";

import { ErrorAlert, PageLoading, TableEmpty } from "@/components/common/feedback";
import { PageHeader } from "@/components/common/page-header";
import { StatusChip } from "@/components/common/status-chip";
import { useAuth } from "@/contexts/auth-context";
import { apiRequest, errorMessage } from "@/lib/api";
import { formatDate, formatMoney } from "@/lib/format";
import type { Asset, CatalogService, Customer, ServiceOrder } from "@/lib/types";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import ArrowForwardRoundedIcon from "@mui/icons-material/ArrowForwardRounded";
import BuildCircleOutlinedIcon from "@mui/icons-material/BuildCircleOutlined";
import DesignServicesOutlinedIcon from "@mui/icons-material/DesignServicesOutlined";
import DevicesOtherRoundedIcon from "@mui/icons-material/DevicesOtherRounded";
import PeopleAltOutlinedIcon from "@mui/icons-material/PeopleAltOutlined";
import TrendingUpRoundedIcon from "@mui/icons-material/TrendingUpRounded";
import {
  Box, Button, Card, CardContent, Grid, IconButton, LinearProgress, Stack, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Typography,
} from "@mui/material";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";

const empty = { customers: [] as Customer[], assets: [] as Asset[], services: [] as CatalogService[], orders: [] as ServiceOrder[] };

export default function DashboardPage() {
  const { user, can } = useAuth();
  const [data, setData] = useState(empty);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const requests = {
        customers: can("CUSTOMER_READ") ? apiRequest<Customer[]>("/customers") : Promise.resolve([]),
        assets: can("ASSET_READ") ? apiRequest<Asset[]>("/assets") : Promise.resolve([]),
        services: can("SERVICE_READ") ? apiRequest<CatalogService[]>("/services") : Promise.resolve([]),
        orders: can("SERVICE_ORDER_READ") ? apiRequest<ServiceOrder[]>("/service-orders") : Promise.resolve([]),
      };
      const [customers, assets, services, orders] = await Promise.all([requests.customers, requests.assets, requests.services, requests.orders]);
      setData({ customers, assets, services, orders });
    } catch (err) { setError(errorMessage(err)); }
    finally { setLoading(false); }
  }, [can]);

  useEffect(() => { load(); }, [load]);
  const openOrders = data.orders.filter((order) => !["COMPLETED", "CANCELLED"].includes(order.status));
  const revenue = data.orders.filter((order) => order.status === "COMPLETED").reduce((sum, order) => sum + Number(order.finalValue ?? order.estimatedValue ?? 0), 0);
  const recentOrders = useMemo(() => [...data.orders].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()).slice(0, 6), [data.orders]);
  const customerMap = useMemo(() => new Map(data.customers.map((item) => [item.id, item.name])), [data.customers]);

  if (loading) return <PageLoading />;

  const stats = [
    { label: "Clientes ativos", value: data.customers.filter((item) => item.status === "ACTIVE").length, helper: `${data.customers.length} cadastrados`, icon: <PeopleAltOutlinedIcon />, color: "#2457E6" },
    { label: "Ativos cadastrados", value: data.assets.length, helper: "Vinculados aos clientes", icon: <DevicesOtherRoundedIcon />, color: "#7A5AF8" },
    { label: "Ordens em aberto", value: openOrders.length, helper: `${data.orders.filter((item) => item.status === "COMPLETED").length} concluídas`, icon: <BuildCircleOutlinedIcon />, color: "#E88D14" },
    { label: "Valor concluído", value: formatMoney(revenue), helper: "Ordens finalizadas", icon: <TrendingUpRoundedIcon />, color: "#16A085" },
  ];

  return (
    <>
      <PageHeader eyebrow="Painel operacional" title={`Bom dia, ${user?.name.split(" ")[0] ?? ""}`} description="Acompanhe os principais indicadores da sua operação.">
        {can("SERVICE_ORDER_CREATE") && <Button component={Link} href="/ordens?nova=1" variant="contained" startIcon={<AddRoundedIcon />}>Nova ordem</Button>}
      </PageHeader>
      {error && <Box mb={3}><ErrorAlert message={error} onRetry={load} /></Box>}
      <Grid container spacing={2.5}>
        {stats.map((stat) => (
          <Grid key={stat.label} size={{ xs: 12, sm: 6, xl: 3 }}>
            <Card sx={{ height: "100%" }}><CardContent sx={{ p: 2.75 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                <Box><Typography color="text.secondary" variant="body2" fontWeight={650}>{stat.label}</Typography><Typography sx={{ mt: 1, fontSize: 29, fontWeight: 800, letterSpacing: "-.03em" }}>{stat.value}</Typography><Typography variant="caption" color="text.secondary">{stat.helper}</Typography></Box>
                <Box sx={{ width: 44, height: 44, display: "grid", placeItems: "center", borderRadius: 2.25, color: stat.color, bgcolor: `${stat.color}14` }}>{stat.icon}</Box>
              </Stack>
            </CardContent></Card>
          </Grid>
        ))}
      </Grid>
      <Grid container spacing={2.5} mt={0.25}>
        <Grid size={{ xs: 12, xl: 8 }}>
          <Card><CardContent sx={{ p: 0 }}>
            <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ p: 2.75, pb: 2 }}>
              <Box><Typography variant="h3">Ordens recentes</Typography><Typography variant="body2" color="text.secondary" mt={0.5}>Últimas movimentações da equipe</Typography></Box>
              <Button component={Link} href="/ordens" endIcon={<ArrowForwardRoundedIcon />}>Ver todas</Button>
            </Stack>
            <TableContainer><Table>
              <TableHead><TableRow><TableCell>Ordem</TableCell><TableCell>Cliente</TableCell><TableCell>Status</TableCell><TableCell>Prazo</TableCell><TableCell align="right">Valor</TableCell></TableRow></TableHead>
              <TableBody>
                {recentOrders.length === 0 && <TableEmpty colSpan={5} message="Nenhuma ordem de serviço criada." />}
                {recentOrders.map((order) => <TableRow key={order.id} hover><TableCell><Typography fontWeight={700} variant="body2">{order.title}</Typography><Typography variant="caption" color="text.secondary">#{order.id.slice(0, 8).toUpperCase()}</Typography></TableCell><TableCell>{customerMap.get(order.customerId) ?? "Cliente"}</TableCell><TableCell><StatusChip value={order.status} /></TableCell><TableCell>{formatDate(order.dueAt)}</TableCell><TableCell align="right" sx={{ fontWeight: 700 }}>{formatMoney(order.finalValue ?? order.estimatedValue)}</TableCell></TableRow>)}
              </TableBody>
            </Table></TableContainer>
          </CardContent></Card>
        </Grid>
        <Grid size={{ xs: 12, xl: 4 }}>
          <Card sx={{ height: "100%" }}><CardContent sx={{ p: 2.75 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}><Box><Typography variant="h3">Fluxo das ordens</Typography><Typography variant="body2" color="text.secondary" mt={0.5}>Distribuição atual</Typography></Box><Box sx={{ width: 42, height: 42, display: "grid", placeItems: "center", borderRadius: 2, bgcolor: "primary.main", color: "white" }}><BuildCircleOutlinedIcon /></Box></Stack>
            {[
              ["Em diagnóstico", data.orders.filter((o) => o.status === "IN_DIAGNOSIS").length, "secondary.main"],
              ["Aguardando aprovação", data.orders.filter((o) => o.status === "WAITING_APPROVAL").length, "warning.main"],
              ["Em andamento", data.orders.filter((o) => o.status === "IN_PROGRESS").length, "primary.main"],
              ["Concluídas", data.orders.filter((o) => o.status === "COMPLETED").length, "success.main"],
            ].map(([label, count, color]) => {
              const value = data.orders.length ? (Number(count) / data.orders.length) * 100 : 0;
              return <Box key={String(label)} mb={2.5}><Stack direction="row" justifyContent="space-between" mb={0.75}><Typography variant="body2" color="text.secondary">{label}</Typography><Typography variant="body2" fontWeight={800}>{count}</Typography></Stack><LinearProgress variant="determinate" value={value} sx={{ height: 7, borderRadius: 99, bgcolor: "#EEF1F6", "& .MuiLinearProgress-bar": { bgcolor: color } }} /></Box>;
            })}
            <Box sx={{ mt: 3, p: 2, borderRadius: 2.5, bgcolor: "#F8FAFC", display: "flex", alignItems: "center", gap: 1.5 }}><DesignServicesOutlinedIcon color="primary" /><Box><Typography variant="body2" fontWeight={750}>{data.services.filter((s) => s.active).length} serviços ativos</Typography><Typography variant="caption" color="text.secondary">Disponíveis no catálogo</Typography></Box><IconButton component={Link} href="/servicos" size="small" sx={{ ml: "auto" }}><ArrowForwardRoundedIcon /></IconButton></Box>
          </CardContent></Card>
        </Grid>
      </Grid>
    </>
  );
}
