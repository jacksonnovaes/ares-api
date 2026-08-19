"use client";

import { ErrorAlert, TableEmpty, TableLoading } from "@/components/common/feedback";
import { PageHeader } from "@/components/common/page-header";
import { StatusChip, enumLabel } from "@/components/common/status-chip";
import {
  QuickAssetDialog, QuickCustomerDialog, QuickServiceDialog, QuickTechnicianDialog,
  RelatedCreateButton,
} from "@/components/quick-create/entity-dialogs";
import { useAuth } from "@/contexts/auth-context";
import { apiRequest, errorMessage } from "@/lib/api";
import { formatDate, formatMoney } from "@/lib/format";
import type { Asset, CatalogService, Customer, ManagedUser, ServiceOrder, ServiceOrderPriority, ServiceOrderStatus } from "@/lib/types";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import ArrowForwardRoundedIcon from "@mui/icons-material/ArrowForwardRounded";
import BuildCircleOutlinedIcon from "@mui/icons-material/BuildCircleOutlined";
import SearchRoundedIcon from "@mui/icons-material/SearchRounded";
import {
  Alert, Box, Button, Card, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, InputAdornment,
  InputLabel, MenuItem, Select, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Typography,
} from "@mui/material";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";

const transitions: Record<ServiceOrderStatus, ServiceOrderStatus[]> = {
  OPEN: ["IN_DIAGNOSIS", "CANCELLED"],
  IN_DIAGNOSIS: ["WAITING_APPROVAL", "IN_PROGRESS", "CANCELLED"],
  WAITING_APPROVAL: ["IN_PROGRESS", "CANCELLED"],
  IN_PROGRESS: ["COMPLETED", "CANCELLED"],
  COMPLETED: [], CANCELLED: [],
};
const blank = { customerId: "", assetId: "", serviceIds: [] as string[], title: "", description: "", priority: "NORMAL" as ServiceOrderPriority, estimatedValue: "", assignedTechnicianId: "", dueAt: "" };

export default function OrdersPage() {
  const { can } = useAuth();
  const [orders, setOrders] = useState<ServiceOrder[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [services, setServices] = useState<CatalogService[]>([]);
  const [users, setUsers] = useState<ManagedUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(blank);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");
  const [statusOrder, setStatusOrder] = useState<ServiceOrder | null>(null);
  const [nextStatus, setNextStatus] = useState<ServiceOrderStatus | "">("");
  const [finalValue, setFinalValue] = useState("");
  const [quickCustomerOpen, setQuickCustomerOpen] = useState(false);
  const [quickAssetOpen, setQuickAssetOpen] = useState(false);
  const [quickServiceOpen, setQuickServiceOpen] = useState(false);
  const [quickTechnicianOpen, setQuickTechnicianOpen] = useState(false);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const [orderData, customerData, assetData, serviceData, userData] = await Promise.all([
        apiRequest<ServiceOrder[]>("/service-orders"),
        can("CUSTOMER_READ") ? apiRequest<Customer[]>("/customers") : Promise.resolve([]),
        can("ASSET_READ") ? apiRequest<Asset[]>("/assets") : Promise.resolve([]),
        can("SERVICE_READ") ? apiRequest<CatalogService[]>("/services") : Promise.resolve([]),
        can("USER_MANAGE") ? apiRequest<ManagedUser[]>("/users") : Promise.resolve([]),
      ]);
      setOrders(orderData); setCustomers(customerData); setAssets(assetData); setServices(serviceData); setUsers(userData);
    } catch (err) { setError(errorMessage(err)); }
    finally { setLoading(false); }
  }, [can]);
  useEffect(() => { load(); }, [load]);
  useEffect(() => { if (new URLSearchParams(window.location.search).get("nova") === "1" && can("SERVICE_ORDER_CREATE")) setOpen(true); }, [can]);

  const customerMap = useMemo(() => new Map(customers.map((item) => [item.id, item.name])), [customers]);
  const assetMap = useMemo(() => new Map(assets.map((item) => [item.id, item.name])), [assets]);
  const filteredAssets = assets.filter((asset) => !form.customerId || asset.customerId === form.customerId);
  const filtered = useMemo(() => orders.filter((order) => {
    const text = `${order.title} ${customerMap.get(order.customerId) ?? ""} ${order.id}`.toLowerCase();
    return text.includes(search.toLowerCase()) && (statusFilter === "all" || order.status === statusFilter);
  }), [customerMap, orders, search, statusFilter]);
  const set = <K extends keyof typeof form>(field: K, value: (typeof form)[K]) => setForm((current) => ({ ...current, [field]: value }));

  function startCreate() { setForm(blank); setFormError(""); setOpen(true); }
  async function submit(event: FormEvent) {
    event.preventDefault(); setSaving(true); setFormError("");
    try {
      const created = await apiRequest<ServiceOrder>("/service-orders", { method: "POST", body: {
        customerId: form.customerId, assetId: form.assetId, serviceIds: form.serviceIds, title: form.title,
        description: form.description || null, priority: form.priority,
        estimatedValue: form.estimatedValue ? Number(form.estimatedValue) : null,
        assignedTechnicianId: form.assignedTechnicianId || null,
        dueAt: form.dueAt ? new Date(form.dueAt).toISOString() : null,
      } });
      setOrders((current) => [created, ...current]); setOpen(false);
    } catch (err) { setFormError(errorMessage(err)); }
    finally { setSaving(false); }
  }

  function startStatus(order: ServiceOrder) { setStatusOrder(order); setNextStatus(""); setFinalValue(order.finalValue?.toString() ?? ""); setFormError(""); }
  async function changeStatus() {
    if (!statusOrder || !nextStatus) return;
    setSaving(true); setFormError("");
    try {
      const updated = await apiRequest<ServiceOrder>(`/service-orders/${statusOrder.id}/status`, { method: "PATCH", body: { status: nextStatus, finalValue: finalValue ? Number(finalValue) : null } });
      setOrders((current) => current.map((item) => item.id === updated.id ? updated : item)); setStatusOrder(null);
    } catch (err) { setFormError(errorMessage(err)); }
    finally { setSaving(false); }
  }

  return (
    <>
      <PageHeader eyebrow="Operação" title="Ordens de serviço" description="Acompanhe cada atendimento do início à conclusão." actionLabel={can("SERVICE_ORDER_CREATE") ? "Nova ordem" : undefined} actionIcon={<AddRoundedIcon />} onAction={startCreate} />
      {error && <Box mb={2.5}><ErrorAlert message={error} onRetry={load} /></Box>}
      <Card>
        <Stack direction={{ xs: "column", md: "row" }} gap={2} sx={{ p: 2.5, borderBottom: "1px solid", borderColor: "divider" }}>
          <TextField placeholder="Buscar por ordem ou cliente" value={search} onChange={(e) => setSearch(e.target.value)} sx={{ flex: 1, maxWidth: 430 }} slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchRoundedIcon color="action" /></InputAdornment> } }} />
          <FormControl sx={{ minWidth: 220 }}><InputLabel>Status</InputLabel><Select label="Status" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}><MenuItem value="all">Todos os status</MenuItem>{Object.keys(transitions).map((status) => <MenuItem value={status} key={status}>{enumLabel(status)}</MenuItem>)}</Select></FormControl>
          <Typography variant="body2" color="text.secondary" alignSelf="center" sx={{ ml: { md: "auto" } }}>{filtered.length} ordens</Typography>
        </Stack>
        <TableContainer><Table>
          <TableHead><TableRow><TableCell>Ordem</TableCell><TableCell>Cliente / ativo</TableCell><TableCell>Prioridade</TableCell><TableCell>Status</TableCell><TableCell>Prazo</TableCell><TableCell align="right">Valor</TableCell>{can("SERVICE_ORDER_UPDATE") && <TableCell align="right">Próxima etapa</TableCell>}</TableRow></TableHead>
          <TableBody>
            {loading && <TableLoading colSpan={7} />}
            {!loading && filtered.length === 0 && <TableEmpty colSpan={7} message="Nenhuma ordem de serviço encontrada." />}
            {filtered.map((order) => <TableRow key={order.id} hover>
              <TableCell><Typography variant="body2" fontWeight={750}>{order.title}</Typography><Typography variant="caption" color="text.secondary">#{order.id.slice(0, 8).toUpperCase()}</Typography></TableCell>
              <TableCell><Typography variant="body2">{customerMap.get(order.customerId) ?? "Cliente"}</Typography><Typography variant="caption" color="text.secondary">{assetMap.get(order.assetId) ?? "Ativo"}</Typography></TableCell>
              <TableCell><StatusChip value={order.priority} /></TableCell><TableCell><StatusChip value={order.status} /></TableCell><TableCell>{formatDate(order.dueAt)}</TableCell><TableCell align="right" sx={{ fontWeight: 700 }}>{formatMoney(order.finalValue ?? order.estimatedValue)}</TableCell>
              {can("SERVICE_ORDER_UPDATE") && <TableCell align="right">{transitions[order.status].length > 0 ? <Button size="small" endIcon={<ArrowForwardRoundedIcon />} onClick={() => startStatus(order)}>Avançar</Button> : <Typography variant="caption" color="text.secondary">Finalizada</Typography>}</TableCell>}
            </TableRow>)}
          </TableBody>
        </Table></TableContainer>
      </Card>
      <Dialog open={open} onClose={() => !saving && setOpen(false)} fullWidth maxWidth="md"><Box component="form" onSubmit={submit}><DialogTitle>Nova ordem de serviço<Typography variant="body2" color="text.secondary" mt={0.5}>Registre a demanda, os serviços e o prazo do atendimento.</Typography></DialogTitle><DialogContent dividers><Stack spacing={2.25}>
        {formError && <Alert severity="error">{formError}</Alert>}
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems="flex-start">
          <Box sx={{ width: "100%" }}><FormControl fullWidth required><InputLabel>Cliente</InputLabel><Select label="Cliente" value={form.customerId} onChange={(e) => { set("customerId", e.target.value); set("assetId", ""); }}>{customers.map((customer) => <MenuItem value={customer.id} key={customer.id}>{customer.name}</MenuItem>)}</Select></FormControl>{can("CUSTOMER_CREATE") && <RelatedCreateButton label="Cadastrar novo cliente" onClick={() => setQuickCustomerOpen(true)} />}</Box>
          <Box sx={{ width: "100%" }}><FormControl fullWidth required disabled={!form.customerId}><InputLabel>Ativo</InputLabel><Select label="Ativo" value={form.assetId} onChange={(e) => set("assetId", e.target.value)}>{filteredAssets.map((asset) => <MenuItem value={asset.id} key={asset.id}>{asset.name}</MenuItem>)}</Select></FormControl>{can("ASSET_CREATE") && <RelatedCreateButton label={form.customerId ? "Cadastrar novo ativo" : "Selecione o cliente para cadastrar um ativo"} disabled={!form.customerId} onClick={() => setQuickAssetOpen(true)} />}</Box>
        </Stack>
        <TextField label="Título da ordem" value={form.title} onChange={(e) => set("title", e.target.value)} required fullWidth autoFocus />
        <TextField label="Descrição do problema / solicitação" value={form.description} onChange={(e) => set("description", e.target.value)} multiline minRows={3} fullWidth />
        <Box><FormControl fullWidth required><InputLabel>Serviços</InputLabel><Select multiple label="Serviços" value={form.serviceIds} onChange={(e) => set("serviceIds", typeof e.target.value === "string" ? e.target.value.split(",") : e.target.value)} renderValue={(selected) => selected.map((id) => services.find((service) => service.id === id)?.name).filter(Boolean).join(", ")}>{services.filter((service) => service.active).map((service) => <MenuItem value={service.id} key={service.id}>{service.name} — {formatMoney(service.basePrice)}</MenuItem>)}</Select></FormControl>{can("SERVICE_CREATE") && <RelatedCreateButton label="Cadastrar novo serviço" onClick={() => setQuickServiceOpen(true)} />}</Box>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}><FormControl fullWidth required><InputLabel>Prioridade</InputLabel><Select label="Prioridade" value={form.priority} onChange={(e) => set("priority", e.target.value as ServiceOrderPriority)}>{["LOW", "NORMAL", "HIGH", "URGENT"].map((priority) => <MenuItem key={priority} value={priority}>{enumLabel(priority)}</MenuItem>)}</Select></FormControl><TextField label="Valor estimado" type="number" value={form.estimatedValue} onChange={(e) => set("estimatedValue", e.target.value)} fullWidth slotProps={{ htmlInput: { min: 0, step: 0.01 }, input: { startAdornment: <InputAdornment position="start">R$</InputAdornment> } }} /></Stack>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems="flex-start">{(users.length > 0 || can("USER_MANAGE")) && <Box sx={{ width: "100%" }}><FormControl fullWidth><InputLabel>Técnico responsável</InputLabel><Select label="Técnico responsável" value={form.assignedTechnicianId} onChange={(e) => set("assignedTechnicianId", e.target.value)}><MenuItem value="">Não atribuído</MenuItem>{users.filter((user) => user.roles.includes("TECHNICIAN") && user.status === "ACTIVE").map((user) => <MenuItem value={user.id} key={user.id}>{user.name}</MenuItem>)}</Select></FormControl>{can("USER_MANAGE") && <RelatedCreateButton label="Cadastrar novo técnico" onClick={() => setQuickTechnicianOpen(true)} />}</Box>}<TextField label="Prazo" type="datetime-local" value={form.dueAt} onChange={(e) => set("dueAt", e.target.value)} fullWidth slotProps={{ inputLabel: { shrink: true } }} /></Stack>
      </Stack></DialogContent><DialogActions sx={{ p: 2.5 }}><Button onClick={() => setOpen(false)} disabled={saving}>Cancelar</Button><Button type="submit" variant="contained" disabled={saving}>{saving ? "Criando..." : "Criar ordem"}</Button></DialogActions></Box></Dialog>
      <Dialog open={Boolean(statusOrder)} onClose={() => !saving && setStatusOrder(null)} fullWidth maxWidth="xs"><DialogTitle>Avançar ordem<Typography variant="body2" color="text.secondary" mt={0.5}>{statusOrder?.title}</Typography></DialogTitle><DialogContent dividers><Stack spacing={2.25}>
        {formError && <Alert severity="error">{formError}</Alert>}
        <Box sx={{ p: 2, borderRadius: 2.5, bgcolor: "#F8FAFC" }}><Typography variant="caption" color="text.secondary">Status atual</Typography><Box mt={0.75}>{statusOrder && <StatusChip value={statusOrder.status} />}</Box></Box>
        <FormControl fullWidth required><InputLabel>Próximo status</InputLabel><Select label="Próximo status" value={nextStatus} onChange={(e) => setNextStatus(e.target.value as ServiceOrderStatus)}>{statusOrder && transitions[statusOrder.status].map((status) => <MenuItem key={status} value={status}>{enumLabel(status)}</MenuItem>)}</Select></FormControl>
        {nextStatus === "COMPLETED" && <TextField label="Valor final" type="number" value={finalValue} onChange={(e) => setFinalValue(e.target.value)} fullWidth required slotProps={{ htmlInput: { min: 0, step: 0.01 }, input: { startAdornment: <InputAdornment position="start">R$</InputAdornment> } }} />}
      </Stack></DialogContent><DialogActions sx={{ p: 2.5 }}><Button onClick={() => setStatusOrder(null)} disabled={saving}>Cancelar</Button><Button variant="contained" onClick={changeStatus} disabled={saving || !nextStatus} startIcon={<BuildCircleOutlinedIcon />}>{saving ? "Atualizando..." : "Confirmar etapa"}</Button></DialogActions></Dialog>
      <QuickCustomerDialog open={quickCustomerOpen} onClose={() => setQuickCustomerOpen(false)} onCreated={(customer) => { setCustomers((current) => [customer, ...current]); set("customerId", customer.id); set("assetId", ""); }} />
      <QuickAssetDialog open={quickAssetOpen} customerId={form.customerId} customerName={customers.find((customer) => customer.id === form.customerId)?.name} onClose={() => setQuickAssetOpen(false)} onCreated={(asset) => { setAssets((current) => [asset, ...current]); set("assetId", asset.id); }} />
      <QuickServiceDialog open={quickServiceOpen} onClose={() => setQuickServiceOpen(false)} onCreated={(service) => { setServices((current) => [service, ...current]); set("serviceIds", [...form.serviceIds, service.id]); }} />
      <QuickTechnicianDialog open={quickTechnicianOpen} onClose={() => setQuickTechnicianOpen(false)} onCreated={(technician) => { setUsers((current) => [technician, ...current]); set("assignedTechnicianId", technician.id); }} />
    </>
  );
}
