"use client";

import {ErrorAlert, PageLoading} from "@/components/common/feedback";
import {PageHeader} from "@/components/common/page-header";
import {useAuth} from "@/contexts/auth-context";
import {apiRequest, errorMessage} from "@/lib/api";
import {formatMoney} from "@/lib/format";
import type {CatalogService} from "@/lib/types";
import AccessTimeRoundedIcon from "@mui/icons-material/AccessTimeRounded";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import DesignServicesOutlinedIcon from "@mui/icons-material/DesignServicesOutlined";
import SearchRoundedIcon from "@mui/icons-material/SearchRounded";
import {
    Alert, Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
    Grid, InputAdornment, Stack, TextField, Typography,
} from "@mui/material";
import {FormEvent, useCallback, useEffect, useMemo, useState} from "react";

const blank = {name: "", description: "", basePrice: "", estimatedMinutes: ""};

export default function ServicesPage() {
    const {can} = useAuth();
    const [items, setItems] = useState<CatalogService[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState(blank);
    const [saving, setSaving] = useState(false);
    const [formError, setFormError] = useState("");

    const load = useCallback(async () => {
        setLoading(true);
        setError("");
        try {
            setItems(await apiRequest<CatalogService[]>("/services"));
        } catch (err) {
            setError(errorMessage(err));
        } finally {
            setLoading(false);
        }
    }, []);
    useEffect(() => {
        load();
    }, [load]);
    const filtered = useMemo(() => items.filter((item) => `${item.name} ${item.description ?? ""}`.toLowerCase().includes(search.toLowerCase())), [items, search]);
    const set = (field: keyof typeof form, value: string) => setForm((current) => ({...current, [field]: value}));

    function startCreate() {
        setForm(blank);
        setFormError("");
        setOpen(true);
    }

    async function submit(event: FormEvent) {
        event.preventDefault();
        setSaving(true);
        setFormError("");
        try {
            const created = await apiRequest<CatalogService>("/services", {
                method: "POST",
                body: {
                    name: form.name,
                    description: form.description || null,
                    basePrice: Number(form.basePrice),
                    estimatedMinutes: form.estimatedMinutes ? Number(form.estimatedMinutes) : null
                }
            });
            setItems((current) => [created, ...current]);
            setOpen(false);
        } catch (err) {
            setFormError(errorMessage(err));
        } finally {
            setSaving(false);
        }
    }

    return (
        <>
            <PageHeader eyebrow="Oferta" title="Catálogo de serviços"
                        description="Padronize o que sua equipe executa, com preço e tempo estimado."
                        actionLabel={can("SERVICE_CREATE") ? "Novo serviço" : undefined} actionIcon={<AddRoundedIcon/>}
                        onAction={startCreate}/>
            {error && <Box mb={2.5}><ErrorAlert message={error} onRetry={load}/></Box>}
            <Stack direction={{xs: "column", sm: "row"}} justifyContent="space-between" gap={2} mb={2.5}>
                <TextField placeholder="Buscar no catálogo" value={search} onChange={(e) => setSearch(e.target.value)}
                           sx={{width: {xs: "100%", sm: 380}}} slotProps={{
                    input: {
                        startAdornment: <InputAdornment position="start"><SearchRoundedIcon
                            color="action"/></InputAdornment>
                    }
                }}/>
                <Typography variant="body2" color="text.secondary"
                            alignSelf="center">{items.filter((item) => item.active).length} serviços ativos</Typography>
            </Stack>
            {loading ? <PageLoading/> : filtered.length === 0 ?
                <Card><CardContent sx={{py: 9, textAlign: "center"}}><DesignServicesOutlinedIcon
                    sx={{fontSize: 48, color: "text.disabled"}}/><Typography variant="h3" mt={1.5}>Catálogo
                    vazio</Typography><Typography color="text.secondary" mt={0.75}>Cadastre os serviços oferecidos pela
                    empresa.</Typography></CardContent></Card> : (
                    <Grid container spacing={2.5}>{filtered.map((service) => <Grid key={service.id}
                                                                                   size={{xs: 12, sm: 6, xl: 4}}><Card
                        sx={{height: "100%"}}><CardContent
                        sx={{p: 2.75, height: "100%", display: "flex", flexDirection: "column"}}>
                        <Stack direction="row" alignItems="center" justifyContent="space-between"><Box sx={{
                            width: 44,
                            height: 44,
                            display: "grid",
                            placeItems: "center",
                            borderRadius: 2.25,
                            color: "primary.main",
                            bgcolor: "primary.main",
                            backgroundColor: "color-mix(in srgb, currentColor 10%, transparent)"
                        }}><DesignServicesOutlinedIcon/></Box><Chip label={service.active ? "Ativo" : "Inativo"}
                                                                    color={service.active ? "success" : "default"}
                                                                    size="small"/></Stack>
                        <Typography variant="h3" mt={2.25}>{service.name}</Typography><Typography variant="body2"
                                                                                                  color="text.secondary"
                                                                                                  mt={0.75}
                                                                                                  sx={{minHeight: 42}}>{service.description || "Sem descrição cadastrada."}</Typography>
                        <Box sx={{mt: "auto", pt: 2.5}}><Stack direction="row" justifyContent="space-between"
                                                               alignItems="flex-end"><Box><Typography variant="caption"
                                                                                                      color="text.secondary">A
                            partir de</Typography><Typography sx={{
                            fontSize: 22,
                            fontWeight: 800,
                            color: "primary.main"
                        }}>{formatMoney(service.basePrice)}</Typography></Box>{service.estimatedMinutes &&
                            <Stack direction="row" spacing={0.75} alignItems="center"
                                   color="text.secondary"><AccessTimeRoundedIcon fontSize="small"/><Typography
                                variant="body2">{service.estimatedMinutes} min</Typography></Stack>}</Stack></Box>
                    </CardContent></Card></Grid>)}</Grid>
                )}
            <Dialog open={open} onClose={() => !saving && setOpen(false)} fullWidth maxWidth="sm"><Box component="form"
                                                                                                       onSubmit={submit}><DialogTitle>Novo
                serviço<Typography variant="body2" color="text.secondary" mt={0.5}>Adicione uma opção ao catálogo da
                    empresa.</Typography></DialogTitle><DialogContent dividers><Stack spacing={2.25}>
                {formError && <Alert severity="error">{formError}</Alert>}
                <TextField label="Nome do serviço" value={form.name} onChange={(e) => set("name", e.target.value)}
                           required fullWidth autoFocus/>
                <TextField label="Descrição" value={form.description}
                           onChange={(e) => set("description", e.target.value)} multiline minRows={3} fullWidth/>
                <Stack direction={{xs: "column", sm: "row"}} spacing={2}><TextField label="Preço base" type="number"
                                                                                    value={form.basePrice}
                                                                                    onChange={(e) => set("basePrice", e.target.value)}
                                                                                    required fullWidth slotProps={{
                    htmlInput: {
                        min: 0,
                        step: 0.01
                    }, input: {startAdornment: <InputAdornment position="start">R$</InputAdornment>}
                }}/><TextField label="Tempo estimado" type="number" value={form.estimatedMinutes}
                               onChange={(e) => set("estimatedMinutes", e.target.value)} fullWidth slotProps={{
                    htmlInput: {min: 1},
                    input: {endAdornment: <InputAdornment position="end">min</InputAdornment>}
                }}/></Stack>
            </Stack></DialogContent><DialogActions sx={{p: 2.5}}><Button onClick={() => setOpen(false)}
                                                                         disabled={saving}>Cancelar</Button><Button
                type="submit" variant="contained" disabled={saving}>{saving ? "Salvando..." : "Salvar serviço"}</Button></DialogActions></Box></Dialog>
        </>
    );
}
