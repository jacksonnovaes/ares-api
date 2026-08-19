"use client";

import { ErrorAlert, TableEmpty, TableLoading } from "@/components/common/feedback";
import { PageHeader } from "@/components/common/page-header";
import { StatusChip, enumLabel } from "@/components/common/status-chip";
import { QuickCustomerDialog, RelatedCreateButton } from "@/components/quick-create/entity-dialogs";
import { useAuth } from "@/contexts/auth-context";
import { apiRequest, errorMessage } from "@/lib/api";
import { initials } from "@/lib/format";
import type { Customer, ManagedUser, Permission, Role, UserStatus } from "@/lib/types";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import MoreHorizRoundedIcon from "@mui/icons-material/MoreHorizRounded";
import {
  Alert, Avatar, Box, Button, Card, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControl, IconButton, InputLabel, ListItemText, Menu, MenuItem, Select, Stack, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from "@mui/material";
import { FormEvent, useCallback, useEffect, useState } from "react";

const roles: Role[] = ["ADMIN", "MANAGER", "ATTENDANT", "TECHNICIAN", "FINANCIAL", "CUSTOMER"];
const permissions: Permission[] = ["CUSTOMER_READ", "CUSTOMER_CREATE", "CUSTOMER_UPDATE", "ASSET_READ", "ASSET_CREATE", "ASSET_UPDATE", "SERVICE_READ", "SERVICE_CREATE", "SERVICE_UPDATE", "SERVICE_ORDER_READ", "SERVICE_ORDER_CREATE", "SERVICE_ORDER_UPDATE", "SERVICE_ORDER_CANCEL", "PAYMENT_READ", "PAYMENT_CREATE", "REPORT_READ", "USER_MANAGE", "TENANT_CONFIGURE"];
const roleLabels: Record<Role, string> = { SUPER_ADMIN: "Superadministrador", ADMIN: "Administrador", MANAGER: "Gerente", ATTENDANT: "Atendente", TECHNICIAN: "Técnico", FINANCIAL: "Financeiro", CUSTOMER: "Cliente" };
const blank = { name: "", email: "", phone: "", jobTitle: "", password: "", confirmation: "", roles: [] as Role[], extraPermissions: [] as Permission[], customerId: "" };

export default function UsersPage() {
  const { can } = useAuth();
  const [items, setItems] = useState<ManagedUser[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(blank);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");
  const [anchor, setAnchor] = useState<null | HTMLElement>(null);
  const [selected, setSelected] = useState<ManagedUser | null>(null);
  const [quickCustomerOpen, setQuickCustomerOpen] = useState(false);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const [usersData, customersData] = await Promise.all([apiRequest<ManagedUser[]>("/users"), apiRequest<Customer[]>("/customers").catch(() => [])]);
      setItems(usersData); setCustomers(customersData);
    } catch (err) { setError(errorMessage(err)); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { load(); }, [load]);
  const set = <K extends keyof typeof form>(field: K, value: (typeof form)[K]) => setForm((current) => ({ ...current, [field]: value }));
  function startCreate() { setForm(blank); setFormError(""); setOpen(true); }
  async function submit(event: FormEvent) {
    event.preventDefault(); setFormError("");
    if (form.password !== form.confirmation) return setFormError("A confirmação da senha não confere.");
    setSaving(true);
    try {
      const created = await apiRequest<ManagedUser>("/users", { method: "POST", body: { name: form.name, email: form.email, phone: form.phone || null, jobTitle: form.jobTitle || null, password: form.password, passwordConfirmation: form.confirmation, roles: form.roles, extraPermissions: form.extraPermissions, customerId: form.roles.includes("CUSTOMER") ? form.customerId || null : null } });
      setItems((current) => [created, ...current]); setOpen(false);
    } catch (err) { setFormError(errorMessage(err)); }
    finally { setSaving(false); }
  }
  function openMenu(event: React.MouseEvent<HTMLElement>, user: ManagedUser) { setAnchor(event.currentTarget); setSelected(user); }
  async function changeStatus(status: UserStatus) {
    if (!selected) return;
    setAnchor(null);
    try {
      const updated = await apiRequest<ManagedUser>(`/users/${selected.id}/status`, { method: "PATCH", body: { status } });
      setItems((current) => current.map((item) => item.id === updated.id ? updated : item));
    } catch (err) { setError(errorMessage(err)); }
  }
  return (
    <>
      <PageHeader eyebrow="Acesso" title="Usuários" description="Gerencie equipe, funções e permissões do ambiente." actionLabel="Novo usuário" actionIcon={<AddRoundedIcon />} onAction={startCreate} />
      {error && <Box mb={2.5}><ErrorAlert message={error} onRetry={load} /></Box>}
      <Card><TableContainer><Table>
        <TableHead><TableRow><TableCell>Usuário</TableCell><TableCell>Cargo</TableCell><TableCell>Perfis</TableCell><TableCell>Permissões</TableCell><TableCell>Status</TableCell><TableCell align="right">Ações</TableCell></TableRow></TableHead>
        <TableBody>
          {loading && <TableLoading colSpan={6} />}{!loading && items.length === 0 && <TableEmpty colSpan={6} message="Nenhum usuário cadastrado." />}
          {items.map((user) => <TableRow key={user.id} hover><TableCell><Stack direction="row" spacing={1.5} alignItems="center"><Avatar sx={{ width: 38, height: 38, bgcolor: "primary.main", fontSize: 13, fontWeight: 800 }}>{initials(user.name)}</Avatar><Box><Typography variant="body2" fontWeight={750}>{user.name}</Typography><Typography variant="caption" color="text.secondary">{user.email}</Typography></Box></Stack></TableCell><TableCell>{user.jobTitle || "—"}</TableCell><TableCell><Stack direction="row" gap={0.5} flexWrap="wrap">{user.roles.map((role) => <Typography key={role} variant="caption" sx={{ px: 1, py: 0.35, borderRadius: 99, bgcolor: "#F0F3F8", fontWeight: 700 }}>{roleLabels[role]}</Typography>)}</Stack></TableCell><TableCell>{user.permissions.length}</TableCell><TableCell><StatusChip value={user.status} /></TableCell><TableCell align="right"><IconButton size="small" onClick={(event) => openMenu(event, user)}><MoreHorizRoundedIcon /></IconButton></TableCell></TableRow>)}
        </TableBody>
      </Table></TableContainer></Card>
      <Menu anchorEl={anchor} open={Boolean(anchor)} onClose={() => setAnchor(null)}>{(["ACTIVE", "BLOCKED", "INACTIVE"] as UserStatus[]).filter((status) => status !== selected?.status).map((status) => <MenuItem key={status} onClick={() => changeStatus(status)}>Marcar como {enumLabel(status).toLowerCase()}</MenuItem>)}</Menu>
      <Dialog open={open} onClose={() => !saving && setOpen(false)} fullWidth maxWidth="md"><Box component="form" onSubmit={submit}><DialogTitle>Novo usuário<Typography variant="body2" color="text.secondary" mt={0.5}>Defina dados de acesso e o escopo de atuação.</Typography>
      </DialogTitle><
        DialogContent dividers>
            <Stack spacing={2.25}>
                {formError && <Alert severity="error">{formError}</Alert>}
                    <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                        <TextField label="Nome completo" required value={form.name} onChange={(e) => set("name", e.target.value)} fullWidth autoFocus />
                        <TextField label="E-mail" type="email" required value={form.email} onChange={(e) => set("email", e.target.value)} fullWidth />
                    </Stack>
                    <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                      <TextField label="Telefone" value={form.phone} onChange={(e) => set("phone", e.target.value)} fullWidth />
                      <TextField label="Cargo" value={form.jobTitle} onChange={(e) => set("jobTitle", e.target.value)} fullWidth /></Stack>
                    <Stack direction={{ xs: "column", sm: "row" }} spacing={2}><TextField label="Senha inicial" type="password" required value={form.password} onChange={(e) => set("password", e.target.value)} fullWidth /><TextField label="Confirmar senha" type="password" required value={form.confirmation} onChange={(e) => set("confirmation", e.target.value)} fullWidth /></Stack>
              <FormControl fullWidth required>
                <InputLabel>Perfis</InputLabel>
                <Select multiple label="Perfis" value={form.roles}
                        onChange={(e) => set("roles", typeof e.target.value === "string" ? e.target.value.split(",") as Role[] : e.target.value as Role[])}
                        renderValue={(selectedRoles) => selectedRoles.map((role) => roleLabels[role]).join(", ")}>{roles.map((role) =>
                    <MenuItem key={role} value={role}>
                        <Checkbox checked={form.roles.includes(role)}/><ListItemText
                        primary={roleLabels[role]}/></MenuItem>)}</Select>
                  </FormControl>
              {form.roles.includes("CUSTOMER") &&
                  <Box>
                      <FormControl fullWidth required><InputLabel>Cliente vinculado</InputLabel><Select
                      label="Cliente vinculado" value={form.customerId}
                      onChange={(e) => set("customerId", e.target.value)}>{customers.map((customer) => <MenuItem
                      value={customer.id}
                      key={customer.id}>{customer.name}</MenuItem>)}</Select></FormControl>{can("CUSTOMER_CREATE") &&
                      <RelatedCreateButton label="Cadastrar novo cliente" onClick={() => setQuickCustomerOpen(true)}/>}
                  </Box>}
              <FormControl fullWidth><InputLabel>Permissões adicionais</InputLabel><Select multiple
                                                                                           label="Permissões adicionais"
                                                                                           value={form.extraPermissions}
                                                                                           onChange={(e) => set("extraPermissions", typeof e.target.value === "string" ? e.target.value.split(",") as Permission[] : e.target.value as Permission[])}
                                                                                           renderValue={(selectedPermissions) => `${selectedPermissions.length} selecionadas`}>{permissions.map((permission) =>
                  <MenuItem key={permission} value={permission}><Checkbox
                      checked={form.extraPermissions.includes(permission)}/><ListItemText
                      primary={enumLabel(permission)}/></MenuItem>)}</Select></FormControl>
            </Stack></DialogContent><DialogActions sx={{p: 2.5}}><Button onClick={() => setOpen(false)}
                                                                         disabled={saving}>Cancelar</Button><Button
          type="submit" variant="contained"
          disabled={saving}>{saving ? "Criando..." : "Criar usuário"}</Button></DialogActions></Box></Dialog>
      <QuickCustomerDialog open={quickCustomerOpen} onClose={() => setQuickCustomerOpen(false)}
                           onCreated={(customer) => {
                             setCustomers((current) => [customer, ...current]);
                             set("customerId", customer.id);
                           }}/>
    </>
  );
}
