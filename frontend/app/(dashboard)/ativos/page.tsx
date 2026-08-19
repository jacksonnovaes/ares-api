"use client";

import { ErrorAlert, PageLoading } from "@/components/common/feedback";
import { PageHeader } from "@/components/common/page-header";
import { QuickCustomerDialog, RelatedCreateButton } from "@/components/quick-create/entity-dialogs";
import { useAuth } from "@/contexts/auth-context";
import { apiRequest, errorMessage } from "@/lib/api";
import type { Asset, AssetType, Customer } from "@/lib/types";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import ComputerRoundedIcon from "@mui/icons-material/ComputerRounded";
import DevicesOtherRoundedIcon from "@mui/icons-material/DevicesOtherRounded";
import DirectionsCarFilledOutlinedIcon from "@mui/icons-material/DirectionsCarFilledOutlined";
import HomeWorkOutlinedIcon from "@mui/icons-material/HomeWorkOutlined";
import PhoneIphoneRoundedIcon from "@mui/icons-material/PhoneIphoneRounded";
import PrecisionManufacturingOutlinedIcon from "@mui/icons-material/PrecisionManufacturingOutlined";
import {
  Alert, Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControl, Grid, InputLabel, MenuItem, Select, Stack, TextField, Typography,
} from "@mui/material";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";

const typeInfo: Record<AssetType, { label: string; icon: React.ReactNode }> = {
  VEHICLE: { label: "Veículo", icon: <DirectionsCarFilledOutlinedIcon /> },
  PHONE: { label: "Celular", icon: <PhoneIphoneRoundedIcon /> },
  COMPUTER: { label: "Computador", icon: <ComputerRoundedIcon /> },
  EQUIPMENT: { label: "Equipamento", icon: <PrecisionManufacturingOutlinedIcon /> },
  PROPERTY: { label: "Imóvel", icon: <HomeWorkOutlinedIcon /> },
  OTHER: { label: "Outro", icon: <DevicesOtherRoundedIcon /> },
};
const blank = { customerId: "", type: "EQUIPMENT" as AssetType, name: "", brand: "", model: "", serialNumber: "", attributes: "" };

export default function AssetsPage() {
  const { can } = useAuth();
  const [assets, setAssets] = useState<Asset[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filter, setFilter] = useState("all");
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(blank);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");
  const [quickCustomerOpen, setQuickCustomerOpen] = useState(false);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const [assetData, customerData] = await Promise.all([
        apiRequest<Asset[]>("/assets"),
        can("CUSTOMER_READ") ? apiRequest<Customer[]>("/customers") : Promise.resolve([]),
      ]);
      setAssets(assetData); setCustomers(customerData);
    } catch (err) { setError(errorMessage(err)); }
    finally { setLoading(false); }
  }, [can]);
  useEffect(() => { load(); }, [load]);
  const customerMap = useMemo(() => new Map(customers.map((item) => [item.id, item.name])), [customers]);
  const filtered = filter === "all" ? assets : assets.filter((asset) => asset.customerId === filter);
  const set = (field: keyof typeof form, value: string) => setForm((current) => ({ ...current, [field]: value }));

  function startCreate() { setForm({ ...blank, customerId: filter !== "all" ? filter : "" }); setFormError(""); setOpen(true); }
  async function submit(event: FormEvent) {
    event.preventDefault(); setSaving(true); setFormError("");
    try {
      const attributes = Object.fromEntries(form.attributes.split("\n").map((line) => line.split(":")).filter((parts) => parts.length >= 2).map(([key, ...value]) => [key.trim(), value.join(":").trim()]));
      const created = await apiRequest<Asset>("/assets", { method: "POST", body: { customerId: form.customerId, type: form.type, name: form.name, brand: form.brand || null, model: form.model || null, serialNumber: form.serialNumber || null, attributes } });
      setAssets((current) => [created, ...current]); setOpen(false);
    } catch (err) { setFormError(errorMessage(err)); }
    finally { setSaving(false); }
  }

  return (
    <>
      <PageHeader eyebrow="Inventário" title="Ativos" description="Equipamentos, veículos e itens vinculados a cada cliente." actionLabel={can("ASSET_CREATE") ? "Novo ativo" : undefined} actionIcon={<AddRoundedIcon />} onAction={startCreate} />
      {error && <Box mb={2.5}><ErrorAlert message={error} onRetry={load} /></Box>}
      <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" alignItems={{ sm: "center" }} gap={2} mb={2.5}>
        <FormControl sx={{ minWidth: { xs: "100%", sm: 300 } }}><InputLabel>Filtrar por cliente</InputLabel><Select label="Filtrar por cliente" value={filter} onChange={(e) => setFilter(e.target.value)}><MenuItem value="all">Todos os clientes</MenuItem>{customers.map((customer) => <MenuItem key={customer.id} value={customer.id}>{customer.name}</MenuItem>)}</Select></FormControl>
        <Typography variant="body2" color="text.secondary">{filtered.length} {filtered.length === 1 ? "ativo encontrado" : "ativos encontrados"}</Typography>
      </Stack>
      {loading ? <PageLoading /> : filtered.length === 0 ? (
        <Card><CardContent sx={{ py: 9, textAlign: "center" }}><DevicesOtherRoundedIcon sx={{ fontSize: 48, color: "text.disabled" }} /><Typography variant="h3" mt={1.5}>Nenhum ativo por aqui</Typography><Typography color="text.secondary" mt={0.75}>Cadastre o primeiro item deste cliente para começar.</Typography>{can("ASSET_CREATE") && <Button variant="outlined" startIcon={<AddRoundedIcon />} sx={{ mt: 2.5 }} onClick={startCreate}>Cadastrar ativo</Button>}</CardContent></Card>
      ) : (
        <Grid container spacing={2.5}>{filtered.map((asset) => {
          const info = typeInfo[asset.type];
          return <Grid key={asset.id} size={{ xs: 12, sm: 6, xl: 4 }}><Card sx={{ height: "100%", transition: "transform .2s, box-shadow .2s", "&:hover": { transform: "translateY(-2px)", boxShadow: "0 12px 30px rgba(16,24,40,.08)" } }}><CardContent sx={{ p: 2.75 }}>
            <Stack direction="row" alignItems="flex-start" justifyContent="space-between"><Box sx={{ width: 46, height: 46, display: "grid", placeItems: "center", borderRadius: 2.25, bgcolor: "primary.main", color: "white" }}>{info.icon}</Box><Chip label={info.label} size="small" variant="outlined" /></Stack>
            <Typography variant="h3" mt={2.25}>{asset.name}</Typography><Typography variant="body2" color="text.secondary" mt={0.5}>{[asset.brand, asset.model].filter(Boolean).join(" • ") || "Sem marca ou modelo"}</Typography>
            <Box sx={{ my: 2.25, borderTop: "1px solid", borderColor: "divider" }} />
            <Stack spacing={1}><Stack direction="row" justifyContent="space-between"><Typography variant="body2" color="text.secondary">Cliente</Typography><Typography variant="body2" fontWeight={700}>{customerMap.get(asset.customerId) ?? "—"}</Typography></Stack><Stack direction="row" justifyContent="space-between"><Typography variant="body2" color="text.secondary">Nº de série</Typography><Typography variant="body2" fontWeight={700}>{asset.serialNumber || "—"}</Typography></Stack></Stack>
            {asset.attributes && Object.keys(asset.attributes).length > 0 && <Stack direction="row" gap={0.75} flexWrap="wrap" mt={2}>{Object.entries(asset.attributes).slice(0, 3).map(([key, value]) => <Chip key={key} label={`${key}: ${value}`} size="small" sx={{ mb: 0.5 }} />)}</Stack>}
          </CardContent></Card></Grid>;
        })}</Grid>
      )}
      <Dialog open={open} onClose={() => !saving && setOpen(false)} fullWidth maxWidth="sm"><Box component="form" onSubmit={submit}><DialogTitle>Novo ativo<Typography variant="body2" color="text.secondary" mt={0.5}>Vincule o item a um cliente e registre sua identificação.</Typography></DialogTitle><DialogContent dividers><Stack spacing={2.25}>
        {formError && <Alert severity="error">{formError}</Alert>}
        <Box><FormControl fullWidth required><InputLabel>Cliente</InputLabel><Select label="Cliente" value={form.customerId} onChange={(e) => set("customerId", e.target.value)}>{customers.map((customer) => <MenuItem key={customer.id} value={customer.id}>{customer.name}</MenuItem>)}</Select></FormControl>{can("CUSTOMER_CREATE") && <RelatedCreateButton label="Cadastrar novo cliente" onClick={() => setQuickCustomerOpen(true)} />}</Box>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}><FormControl fullWidth required><InputLabel>Tipo</InputLabel><Select label="Tipo" value={form.type} onChange={(e) => set("type", e.target.value)}>{Object.entries(typeInfo).map(([value, info]) => <MenuItem key={value} value={value}>{info.label}</MenuItem>)}</Select></FormControl><TextField label="Nome do ativo" value={form.name} onChange={(e) => set("name", e.target.value)} required fullWidth /></Stack>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}><TextField label="Marca" value={form.brand} onChange={(e) => set("brand", e.target.value)} fullWidth /><TextField label="Modelo" value={form.model} onChange={(e) => set("model", e.target.value)} fullWidth /></Stack>
        <TextField label="Número de série / identificação" value={form.serialNumber} onChange={(e) => set("serialNumber", e.target.value)} fullWidth />
        <TextField label="Atributos personalizados" value={form.attributes} onChange={(e) => set("attributes", e.target.value)} placeholder={"Cor: Preto\nAno: 2025"} helperText="Use uma linha por atributo no formato Chave: valor." multiline minRows={3} fullWidth />
      </Stack></DialogContent><DialogActions sx={{ p: 2.5 }}><Button onClick={() => setOpen(false)} disabled={saving}>Cancelar</Button><Button type="submit" variant="contained" disabled={saving || customers.length === 0}>{saving ? "Salvando..." : "Salvar ativo"}</Button></DialogActions></Box></Dialog>
      <QuickCustomerDialog open={quickCustomerOpen} onClose={() => setQuickCustomerOpen(false)} onCreated={(customer) => { setCustomers((current) => [customer, ...current]); set("customerId", customer.id); }} />
    </>
  );
}
