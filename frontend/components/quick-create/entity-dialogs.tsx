"use client";

import { apiRequest, errorMessage } from "@/lib/api";
import type { Asset, AssetType, CatalogService, Customer, CustomerType, ManagedUser } from "@/lib/types";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, FormControl,
  InputAdornment, InputLabel, MenuItem, Select, Stack, TextField, Typography,
} from "@mui/material";
import { FormEvent, useEffect, useState } from "react";

export function RelatedCreateButton({ label, onClick, disabled = false }: { label: string; onClick: () => void; disabled?: boolean }) {
  return (
    <Button
      type="button"
      size="small"
      startIcon={<AddRoundedIcon />}
      onClick={onClick}
      disabled={disabled}
      sx={{ alignSelf: "flex-start", px: 0.5, minHeight: 30 }}
    >
      {label}
    </Button>
  );
}

const customerInitial = { type: "PERSON" as CustomerType, name: "", document: "", email: "", phone: "", notes: "" };

export function QuickCustomerDialog({ open, onClose, onCreated }: { open: boolean; onClose: () => void; onCreated: (customer: Customer) => void }) {
  const [form, setForm] = useState(customerInitial);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  useEffect(() => { if (open) { setForm(customerInitial); setError(""); } }, [open]);
  const set = (field: keyof typeof form, value: string) => setForm((current) => ({ ...current, [field]: value }));

  async function submit(event: FormEvent) {
    event.preventDefault(); event.stopPropagation(); setSaving(true); setError("");
    try {
      const created = await apiRequest<Customer>("/customers", { method: "POST", body: {
        type: form.type, name: form.name, document: form.document || null, email: form.email || null,
        phone: form.phone || null, notes: form.notes || null,
      } });
      onCreated(created); onClose();
    } catch (err) { setError(errorMessage(err)); }
    finally { setSaving(false); }
  }

  return (
    <Dialog open={open} onClose={() => !saving && onClose()} fullWidth maxWidth="sm">
      <Box component="form" onSubmit={submit}>
        <DialogTitle>Cadastro rápido de cliente<Typography variant="body2" color="text.secondary" mt={0.5}>Ao salvar, o novo cliente será selecionado automaticamente.</Typography></DialogTitle>
        <DialogContent dividers><Stack spacing={2.25}>
          {error && <Alert severity="error">{error}</Alert>}
          <FormControl fullWidth><InputLabel>Tipo</InputLabel><Select value={form.type} label="Tipo" onChange={(event) => set("type", event.target.value)}><MenuItem value="PERSON">Pessoa física</MenuItem><MenuItem value="COMPANY">Empresa</MenuItem></Select></FormControl>
          <TextField label={form.type === "COMPANY" ? "Nome da empresa" : "Nome completo"} value={form.name} onChange={(event) => set("name", event.target.value)} required fullWidth autoFocus />
          <TextField label={form.type === "COMPANY" ? "CNPJ" : "CPF"} value={form.document} onChange={(event) => set("document", event.target.value)} fullWidth />
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}><TextField label="E-mail" type="email" value={form.email} onChange={(event) => set("email", event.target.value)} fullWidth /><TextField label="Telefone" value={form.phone} onChange={(event) => set("phone", event.target.value)} fullWidth /></Stack>
          <TextField label="Observações" value={form.notes} onChange={(event) => set("notes", event.target.value)} multiline minRows={2} fullWidth />
        </Stack></DialogContent>
        <DialogActions sx={{ p: 2.5 }}><Button type="button" onClick={onClose} disabled={saving}>Cancelar</Button><Button type="submit" variant="contained" disabled={saving}>{saving ? "Salvando..." : "Cadastrar e selecionar"}</Button></DialogActions>
      </Box>
    </Dialog>
  );
}

const assetInitial = { type: "EQUIPMENT" as AssetType, name: "", brand: "", model: "", serialNumber: "", attributes: "" };
const assetTypeLabels: Record<AssetType, string> = { VEHICLE: "Veículo", PHONE: "Celular", COMPUTER: "Computador", EQUIPMENT: "Equipamento", PROPERTY: "Imóvel", OTHER: "Outro" };

export function QuickAssetDialog({ open, customerId, customerName, onClose, onCreated }: { open: boolean; customerId: string; customerName?: string; onClose: () => void; onCreated: (asset: Asset) => void }) {
  const [form, setForm] = useState(assetInitial);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  useEffect(() => { if (open) { setForm(assetInitial); setError(""); } }, [open]);
  const set = (field: keyof typeof form, value: string) => setForm((current) => ({ ...current, [field]: value }));

  async function submit(event: FormEvent) {
    event.preventDefault(); event.stopPropagation(); setSaving(true); setError("");
    try {
      const attributes = Object.fromEntries(form.attributes.split("\n").map((line) => line.split(":"))
        .filter((parts) => parts.length >= 2).map(([key, ...value]) => [key.trim(), value.join(":").trim()]));
      const created = await apiRequest<Asset>("/assets", { method: "POST", body: {
        customerId, type: form.type, name: form.name, brand: form.brand || null, model: form.model || null,
        serialNumber: form.serialNumber || null, attributes,
      } });
      onCreated(created); onClose();
    } catch (err) { setError(errorMessage(err)); }
    finally { setSaving(false); }
  }

  return (
    <Dialog open={open} onClose={() => !saving && onClose()} fullWidth maxWidth="sm">
      <Box component="form" onSubmit={submit}>
        <DialogTitle>Cadastro rápido de ativo<Typography variant="body2" color="text.secondary" mt={0.5}>Cliente: {customerName || "selecionado na ordem"}</Typography></DialogTitle>
        <DialogContent dividers><Stack spacing={2.25}>
          {error && <Alert severity="error">{error}</Alert>}
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}><FormControl fullWidth required><InputLabel>Tipo</InputLabel><Select label="Tipo" value={form.type} onChange={(event) => set("type", event.target.value)}>{Object.entries(assetTypeLabels).map(([value, label]) => <MenuItem key={value} value={value}>{label}</MenuItem>)}</Select></FormControl><TextField label="Nome do ativo" value={form.name} onChange={(event) => set("name", event.target.value)} required fullWidth autoFocus /></Stack>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}><TextField label="Marca" value={form.brand} onChange={(event) => set("brand", event.target.value)} fullWidth /><TextField label="Modelo" value={form.model} onChange={(event) => set("model", event.target.value)} fullWidth /></Stack>
          <TextField label="Número de série / identificação" value={form.serialNumber} onChange={(event) => set("serialNumber", event.target.value)} fullWidth />
          <TextField label="Atributos personalizados" value={form.attributes} onChange={(event) => set("attributes", event.target.value)} placeholder={"Cor: Preto\nAno: 2025"} helperText="Uma linha por atributo, no formato Chave: valor." multiline minRows={2} fullWidth />
        </Stack></DialogContent>
        <DialogActions sx={{ p: 2.5 }}><Button type="button" onClick={onClose} disabled={saving}>Cancelar</Button><Button type="submit" variant="contained" disabled={saving || !customerId}>{saving ? "Salvando..." : "Cadastrar e selecionar"}</Button></DialogActions>
      </Box>
    </Dialog>
  );
}

const serviceInitial = { name: "", description: "", basePrice: "", estimatedMinutes: "" };

export function QuickServiceDialog({ open, onClose, onCreated }: { open: boolean; onClose: () => void; onCreated: (service: CatalogService) => void }) {
  const [form, setForm] = useState(serviceInitial);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  useEffect(() => { if (open) { setForm(serviceInitial); setError(""); } }, [open]);
  const set = (field: keyof typeof form, value: string) => setForm((current) => ({ ...current, [field]: value }));
  async function submit(event: FormEvent) {
    event.preventDefault(); event.stopPropagation(); setSaving(true); setError("");
    try {
      const created = await apiRequest<CatalogService>("/services", { method: "POST", body: {
        name: form.name, description: form.description || null, basePrice: Number(form.basePrice),
        estimatedMinutes: form.estimatedMinutes ? Number(form.estimatedMinutes) : null,
      } });
      onCreated(created); onClose();
    } catch (err) { setError(errorMessage(err)); }
    finally { setSaving(false); }
  }
  return (
    <Dialog open={open} onClose={() => !saving && onClose()} fullWidth maxWidth="sm"><Box component="form" onSubmit={submit}>
      <DialogTitle>Cadastro rápido de serviço<Typography variant="body2" color="text.secondary" mt={0.5}>O serviço será incluído na ordem atual.</Typography></DialogTitle>
      <DialogContent dividers><Stack spacing={2.25}>{error && <Alert severity="error">{error}</Alert>}<TextField label="Nome do serviço" value={form.name} onChange={(event) => set("name", event.target.value)} required fullWidth autoFocus /><TextField label="Descrição" value={form.description} onChange={(event) => set("description", event.target.value)} multiline minRows={2} fullWidth /><Stack direction={{ xs: "column", sm: "row" }} spacing={2}><TextField label="Preço base" type="number" value={form.basePrice} onChange={(event) => set("basePrice", event.target.value)} required fullWidth slotProps={{ htmlInput: { min: 0, step: 0.01 }, input: { startAdornment: <InputAdornment position="start">R$</InputAdornment> } }} /><TextField label="Tempo estimado" type="number" value={form.estimatedMinutes} onChange={(event) => set("estimatedMinutes", event.target.value)} fullWidth slotProps={{ htmlInput: { min: 1 }, input: { endAdornment: <InputAdornment position="end">min</InputAdornment> } }} /></Stack></Stack></DialogContent>
      <DialogActions sx={{ p: 2.5 }}><Button type="button" onClick={onClose} disabled={saving}>Cancelar</Button><Button type="submit" variant="contained" disabled={saving}>{saving ? "Salvando..." : "Cadastrar e incluir"}</Button></DialogActions>
    </Box></Dialog>
  );
}

const technicianInitial = { name: "", email: "", phone: "", jobTitle: "Técnico", password: "", confirmation: "" };

export function QuickTechnicianDialog({ open, onClose, onCreated }: { open: boolean; onClose: () => void; onCreated: (user: ManagedUser) => void }) {
  const [form, setForm] = useState(technicianInitial);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  useEffect(() => { if (open) { setForm(technicianInitial); setError(""); } }, [open]);
  const set = (field: keyof typeof form, value: string) => setForm((current) => ({ ...current, [field]: value }));
  async function submit(event: FormEvent) {
    event.preventDefault(); event.stopPropagation(); setError("");
    if (form.password !== form.confirmation) return setError("A confirmação da senha não confere.");
    setSaving(true);
    try {
      const created = await apiRequest<ManagedUser>("/users", { method: "POST", body: {
        name: form.name, email: form.email, phone: form.phone || null, jobTitle: form.jobTitle || null,
        password: form.password, passwordConfirmation: form.confirmation, roles: ["TECHNICIAN"],
        extraPermissions: [], customerId: null,
      } });
      onCreated(created); onClose();
    } catch (err) { setError(errorMessage(err)); }
    finally { setSaving(false); }
  }
  return (
    <Dialog open={open} onClose={() => !saving && onClose()} fullWidth maxWidth="sm"><Box component="form" onSubmit={submit}>
      <DialogTitle>Cadastro rápido de técnico<Typography variant="body2" color="text.secondary" mt={0.5}>O técnico será atribuído à ordem atual.</Typography></DialogTitle>
      <DialogContent dividers><Stack spacing={2.25}>{error && <Alert severity="error">{error}</Alert>}<Stack direction={{ xs: "column", sm: "row" }} spacing={2}><TextField label="Nome completo" value={form.name} onChange={(event) => set("name", event.target.value)} required fullWidth autoFocus /><TextField label="Cargo" value={form.jobTitle} onChange={(event) => set("jobTitle", event.target.value)} fullWidth /></Stack><Stack direction={{ xs: "column", sm: "row" }} spacing={2}><TextField label="E-mail" type="email" value={form.email} onChange={(event) => set("email", event.target.value)} required fullWidth /><TextField label="Telefone" value={form.phone} onChange={(event) => set("phone", event.target.value)} fullWidth /></Stack><Stack direction={{ xs: "column", sm: "row" }} spacing={2}><TextField label="Senha inicial" type="password" value={form.password} onChange={(event) => set("password", event.target.value)} required fullWidth /><TextField label="Confirmar senha" type="password" value={form.confirmation} onChange={(event) => set("confirmation", event.target.value)} required fullWidth /></Stack></Stack></DialogContent>
      <DialogActions sx={{ p: 2.5 }}><Button type="button" onClick={onClose} disabled={saving}>Cancelar</Button><Button type="submit" variant="contained" disabled={saving}>{saving ? "Salvando..." : "Cadastrar e atribuir"}</Button></DialogActions>
    </Box></Dialog>
  );
}
