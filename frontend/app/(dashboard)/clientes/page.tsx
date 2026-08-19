"use client";

import { ErrorAlert, TableEmpty, TableLoading } from "@/components/common/feedback";
import { PageHeader } from "@/components/common/page-header";
import { StatusChip } from "@/components/common/status-chip";
import { useAuth } from "@/contexts/auth-context";
import { apiRequest, errorMessage } from "@/lib/api";
import { initials, maskDocument } from "@/lib/format";
import type { Customer, CustomerType } from "@/lib/types";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import BusinessOutlinedIcon from "@mui/icons-material/BusinessOutlined";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import PersonOutlineRoundedIcon from "@mui/icons-material/PersonOutlineRounded";
import SearchRoundedIcon from "@mui/icons-material/SearchRounded";
import {
  Alert, Avatar, Box, Button, Card, Dialog, DialogActions, DialogContent, DialogTitle, FormControl,
  IconButton, InputAdornment, InputLabel, MenuItem, Select, Stack, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, TextField, Tooltip, Typography,
} from "@mui/material";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";

const blank = { type: "PERSON" as CustomerType, name: "", document: "", email: "", phone: "", notes: "" };

export default function CustomersPage() {
  const { can } = useAuth();
  const [items, setItems] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Customer | null>(null);
  const [form, setForm] = useState(blank);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try { setItems(await apiRequest<Customer[]>("/customers")); }
    catch (err) { setError(errorMessage(err)); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const filtered = useMemo(() => {
    const term = search.toLowerCase();
    return items.filter((item) => [item.name, item.email, item.document, item.phone].some((value) => value?.toLowerCase().includes(term)));
  }, [items, search]);

  function startCreate() { setEditing(null); setForm(blank); setFormError(""); setOpen(true); }
  function startEdit(customer: Customer) {
    setEditing(customer); setForm({ type: customer.type, name: customer.name, document: customer.document ?? "", email: customer.email ?? "", phone: customer.phone ?? "", notes: customer.notes ?? "" }); setFormError(""); setOpen(true);
  }
  const set = (field: keyof typeof form, value: string) => setForm((current) => ({ ...current, [field]: value }));

  async function submit(event: FormEvent) {
    event.preventDefault(); setSaving(true); setFormError("");
    try {
      const body = editing
        ? { name: form.name, email: form.email || null, phone: form.phone || null, notes: form.notes || null }
        : { ...form, document: form.document || null, email: form.email || null, phone: form.phone || null, notes: form.notes || null };
      const saved = await apiRequest<Customer>(editing ? `/customers/${editing.id}` : "/customers", { method: editing ? "PUT" : "POST", body });
      setItems((current) => editing ? current.map((item) => item.id === saved.id ? saved : item) : [saved, ...current]);
      setOpen(false);
    } catch (err) { setFormError(errorMessage(err)); }
    finally { setSaving(false); }
  }

  return (
    <>
      <PageHeader eyebrow="Relacionamento" title="Clientes" description="Centralize contatos, documentos e histórico da sua base." actionLabel={can("CUSTOMER_CREATE") ? "Novo cliente" : undefined} actionIcon={<AddRoundedIcon />} onAction={startCreate} />
      {error && <Box mb={2.5}><ErrorAlert message={error} onRetry={load} /></Box>}
      <Card>
        <Stack direction={{ xs: "column", sm: "row" }} gap={2} justifyContent="space-between" sx={{ p: 2.5, borderBottom: "1px solid", borderColor: "divider" }}>
          <TextField placeholder="Buscar por nome, e-mail ou documento" value={search} onChange={(e) => setSearch(e.target.value)} sx={{ width: { xs: "100%", sm: 390 } }} slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchRoundedIcon color="action" /></InputAdornment> } }} />
          <Typography variant="body2" color="text.secondary" alignSelf="center">{filtered.length} {filtered.length === 1 ? "cliente" : "clientes"}</Typography>
        </Stack>
        <TableContainer><Table>
          <TableHead><TableRow><TableCell>Cliente</TableCell><TableCell>Tipo</TableCell><TableCell>Documento</TableCell><TableCell>Contato</TableCell><TableCell>Status</TableCell>{can("CUSTOMER_UPDATE") && <TableCell align="right">Ações</TableCell>}</TableRow></TableHead>
          <TableBody>
            {loading && <TableLoading colSpan={6} />}
            {!loading && filtered.length === 0 && <TableEmpty colSpan={6} message={search ? "Nenhum cliente corresponde à busca." : "Cadastre seu primeiro cliente."} />}
            {filtered.map((customer) => (
              <TableRow key={customer.id} hover>
                <TableCell><Stack direction="row" spacing={1.5} alignItems="center"><Avatar sx={{ width: 38, height: 38, bgcolor: customer.type === "COMPANY" ? "secondary.main" : "primary.main", fontSize: 13, fontWeight: 800 }}>{initials(customer.name)}</Avatar><Box><Typography variant="body2" fontWeight={750}>{customer.name}</Typography><Typography variant="caption" color="text.secondary">{customer.email || "Sem e-mail"}</Typography></Box></Stack></TableCell>
                <TableCell><Stack direction="row" spacing={0.75} alignItems="center">{customer.type === "COMPANY" ? <BusinessOutlinedIcon fontSize="small" color="action" /> : <PersonOutlineRoundedIcon fontSize="small" color="action" />}<Typography variant="body2">{customer.type === "COMPANY" ? "Empresa" : "Pessoa"}</Typography></Stack></TableCell>
                <TableCell>{maskDocument(customer.document)}</TableCell><TableCell>{customer.phone || "—"}</TableCell><TableCell><StatusChip value={customer.status} /></TableCell>
                {can("CUSTOMER_UPDATE") && <TableCell align="right"><Tooltip title="Editar cliente"><IconButton size="small" onClick={() => startEdit(customer)}><EditOutlinedIcon fontSize="small" /></IconButton></Tooltip></TableCell>}
              </TableRow>
            ))}
          </TableBody>
        </Table></TableContainer>
      </Card>
      <Dialog open={open} onClose={() => !saving && setOpen(false)} fullWidth maxWidth="sm">
        <Box component="form" onSubmit={submit}>
          <DialogTitle>{editing ? "Editar cliente" : "Novo cliente"}<Typography variant="body2" color="text.secondary" mt={0.5}>{editing ? "Atualize os dados de contato." : "Adicione uma pessoa ou empresa à sua base."}</Typography></DialogTitle>
          <DialogContent dividers><Stack spacing={2.25}>
            {formError && <Alert severity="error">{formError}</Alert>}
            {!editing && <FormControl fullWidth><InputLabel>Tipo</InputLabel><Select value={form.type} label="Tipo" onChange={(e) => set("type", e.target.value)}><MenuItem value="PERSON">Pessoa física</MenuItem><MenuItem value="COMPANY">Empresa</MenuItem></Select></FormControl>}
            <TextField label={form.type === "COMPANY" ? "Nome da empresa" : "Nome completo"} value={form.name} onChange={(e) => set("name", e.target.value)} required fullWidth autoFocus />
            {!editing && <TextField label={form.type === "COMPANY" ? "CNPJ" : "CPF"} value={form.document} onChange={(e) => set("document", e.target.value)} fullWidth />}
            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}><TextField label="E-mail" type="email" value={form.email} onChange={(e) => set("email", e.target.value)} fullWidth /><TextField label="Telefone" value={form.phone} onChange={(e) => set("phone", e.target.value)} fullWidth /></Stack>
            <TextField label="Observações" value={form.notes} onChange={(e) => set("notes", e.target.value)} multiline minRows={3} fullWidth />
          </Stack></DialogContent>
          <DialogActions sx={{ p: 2.5 }}><Button onClick={() => setOpen(false)} disabled={saving}>Cancelar</Button><Button type="submit" variant="contained" disabled={saving}>{saving ? "Salvando..." : "Salvar cliente"}</Button></DialogActions>
        </Box>
      </Dialog>
    </>
  );
}
