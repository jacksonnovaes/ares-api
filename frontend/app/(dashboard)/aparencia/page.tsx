"use client";

import {PageHeader} from "@/components/common/page-header";
import {useAuth} from "@/contexts/auth-context";
import {BrandSettings, useBrand} from "@/contexts/brand-context";
import CheckRoundedIcon from "@mui/icons-material/CheckRounded";
import CloudDownloadOutlinedIcon from "@mui/icons-material/CloudDownloadOutlined";
import ImageOutlinedIcon from "@mui/icons-material/ImageOutlined";
import PaletteOutlinedIcon from "@mui/icons-material/PaletteOutlined";
import RestartAltRoundedIcon from "@mui/icons-material/RestartAltRounded";
import UploadRoundedIcon from "@mui/icons-material/UploadRounded";
import {
    Alert, Box, Button, Card, CardContent, Divider, Grid, InputAdornment, Slider, Stack, TextField,
    Typography,
} from "@mui/material";
import {ChangeEvent, FormEvent, useEffect, useState} from "react";

function ColorField({label, value, onChange}: { label: string; value: string; onChange: (value: string) => void }) {
    return <TextField label={label} value={value} onChange={(event) => onChange(event.target.value)} fullWidth
                      slotProps={{
                          input: {
                              startAdornment: <InputAdornment position="start"><Box component="input" type="color"
                                                                                    value={/^#[0-9a-f]{6}$/i.test(value) ? value : "#2457E6"}
                                                                                    onChange={(event) => onChange(event.target.value)}
                                                                                    aria-label={`Selecionar ${label.toLowerCase()}`}
                                                                                    sx={{
                                                                                        width: 28,
                                                                                        height: 28,
                                                                                        p: 0,
                                                                                        border: 0,
                                                                                        bgcolor: "transparent",
                                                                                        cursor: "pointer"
                                                                                    }}/></InputAdornment>
                          }
                      }}/>;
}

export default function AppearancePage() {
    const {user} = useAuth();
    const {brand, remoteBrand, saveBrand, restoreRemoteBrand, loadBranding} = useBrand();
    const [form, setForm] = useState<BrandSettings>(brand);
    const [saved, setSaved] = useState(false);
    useEffect(() => setForm(brand), [brand]);
    const set = <K extends keyof BrandSettings>(field: K, value: BrandSettings[K]) => setForm((current) => ({
        ...current,
        [field]: value
    }));

    function upload(event: ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0];
        if (!file) return;
        if (file.size > 1024 * 1024) return;
        const reader = new FileReader();
        reader.onload = () => set("logoUrl", String(reader.result));
        reader.readAsDataURL(file);
    }

    function submit(event: FormEvent) {
        event.preventDefault();
        saveBrand(form);
        setSaved(true);
        window.setTimeout(() => setSaved(false), 2800);
    }

    async function syncRemote() {
        if (!user?.tenant.slug) return;
        await loadBranding(user.tenant.slug);
    }

    function restore() {
        restoreRemoteBrand();
        setSaved(false);
    }

    return (
        <>
            <PageHeader eyebrow="Whitelabel" title="Aparência"
                        description="Personalize a marca exibida para sua equipe neste ambiente."/>
            <Alert severity="info" sx={{mb: 3}}>A API atual oferece consulta da identidade, mas ainda não possui
                endpoint para atualizá-la. Por isso, estas preferências ficam salvas neste navegador e são aplicadas
                imediatamente.</Alert>
            <Grid container spacing={3}>
                <Grid size={{xs: 12, lg: 7}}>
                    <Card><Box component="form" onSubmit={submit}><CardContent sx={{p: {xs: 2.5, sm: 3.5}}}>
                        <Stack direction="row" spacing={1.5} alignItems="center" mb={3}><Box sx={{
                            width: 42,
                            height: 42,
                            display: "grid",
                            placeItems: "center",
                            borderRadius: 2,
                            bgcolor: "primary.main",
                            color: "white"
                        }}><PaletteOutlinedIcon/></Box><Box><Typography variant="h3">Identidade
                            visual</Typography><Typography variant="body2" color="text.secondary">Nome, logo e paleta
                            principal.</Typography></Box></Stack>
                        <Stack spacing={2.5}>
                            {saved && <Alert severity="success" icon={<CheckRoundedIcon/>}>Identidade visual salva e
                                aplicada.</Alert>}
                            <TextField label="Nome da marca" value={form.tradeName}
                                       onChange={(e) => set("tradeName", e.target.value)} required fullWidth/>
                            <Box><Typography variant="body2" fontWeight={700} mb={1}>Logo</Typography><Stack
                                direction={{xs: "column", sm: "row"}} spacing={2} alignItems={{sm: "center"}}>
                                <Box sx={{
                                    width: 84,
                                    height: 84,
                                    borderRadius: 3,
                                    border: "1px dashed",
                                    borderColor: "divider",
                                    bgcolor: "#F8FAFC",
                                    display: "grid",
                                    placeItems: "center",
                                    overflow: "hidden",
                                    flexShrink: 0
                                }}>{form.logoUrl ? <Box component="img" src={form.logoUrl} alt="Prévia do logo" sx={{
                                    width: "100%",
                                    height: "100%",
                                    objectFit: "contain",
                                    p: 1
                                }}/> : <ImageOutlinedIcon sx={{color: "text.disabled", fontSize: 34}}/>}</Box>
                                <Stack spacing={1.25} flex={1}>
                                    <TextField label="URL do logo"
                                               value={form.logoUrl.startsWith("data:") ? "Logo carregado do dispositivo" : form.logoUrl}
                                               onChange={(e) => set("logoUrl", e.target.value)}
                                               disabled={form.logoUrl.startsWith("data:")}
                                               fullWidth/><Stack direction="row" spacing={1}><Button
                                    component="label" variant="outlined" startIcon={<UploadRoundedIcon/>}>Enviar arquivo<input
                                    hidden type="file" accept="image/png,image/jpeg,image/webp"
                                    onChange={upload}/></Button>{form.logoUrl &&
                                    <Button color="inherit" onClick={() => set("logoUrl", "")}>Remover</Button>}</Stack><Typography
                                    variant="caption" color="text.secondary">PNG, JPG ou WebP de até 1 MB.</Typography></Stack>
                            </Stack></Box>
                            <Divider/>
                            <Typography variant="h3">Cores e formas</Typography>
                            <Stack direction={{xs: "column", sm: "row"}} spacing={2}>
                                <ColorField label="Cor principal"
                                            value={form.primaryColor}
                                            onChange={(value) => set("primaryColor", value)}/>
                                <ColorField
                                    label="Cor de destaque" value={form.secondaryColor}
                                    onChange={(value) => set("secondaryColor", value)}/>
                            </Stack>
                            <Box>
                                <Stack direction="row" justifyContent="space-between">
                                    <Typography variant="body2" fontWeight={700}>Arredondamento</Typography>
                                    <Typography variant="body2"
                                                color="text.secondary">{form.borderRadius}px</Typography>
                                </Stack>
                                <Slider
                                    value={form.borderRadius}
                                    onChange={(_, value) => set("borderRadius", Number(value))}
                                    min={6} max={24} step={2} marks sx={{mt: 2}}/></Box>
                            <Stack direction={{xs: "column", sm: "row"}} spacing={1.5}
                                   justifyContent="space-between"><Stack direction="row" spacing={1}><Button
                                color="inherit" variant="outlined" startIcon={<RestartAltRoundedIcon/>}
                                onClick={restore}>Restaurar cadastro</Button>{remoteBrand &&
                                <Button color="inherit" startIcon={<CloudDownloadOutlinedIcon/>}
                                        onClick={syncRemote}>Sincronizar</Button>}</Stack><Button type="submit"
                                                                                                  variant="contained"
                                                                                                  startIcon={
                                                                                                      <CheckRoundedIcon/>}>Salvar
                                aparência</Button></Stack>
                        </Stack>
                    </CardContent></Box></Card>
                </Grid>
                <Grid size={{xs: 12, lg: 5}}>
                    <Box sx={{position: {lg: "sticky"}, top: {lg: 104}}}>
                        <Typography variant="h3" mb={1}>Prévia ao vivo</Typography><Typography variant="body2"
                                                                                               color="text.secondary"
                                                                                               mb={2}>A aparência final
                        será aplicada ao salvar.</Typography>
                        <Card sx={{overflow: "hidden", borderRadius: `${form.borderRadius}px`}}>
                            <Box sx={{
                                height: 150,
                                p: 3,
                                color: "white",
                                background: `linear-gradient(135deg, ${form.primaryColor}, ${form.secondaryColor})`,
                                display: "flex",
                                flexDirection: "column",
                                justifyContent: "space-between"
                            }}><Stack direction="row" alignItems="center" spacing={1.25}>{form.logoUrl ?
                                <Box component="img" src={form.logoUrl} alt="Logo na prévia" sx={{
                                    width: 36,
                                    height: 36,
                                    objectFit: "contain",
                                    borderRadius: 1.5,
                                    bgcolor: "white"
                                }}/> : <Box sx={{
                                    width: 36,
                                    height: 36,
                                    borderRadius: 1.5,
                                    display: "grid",
                                    placeItems: "center",
                                    bgcolor: "rgba(255,255,255,.2)",
                                    fontWeight: 900
                                }}>{form.tradeName.charAt(0).toUpperCase() || "A"}</Box>}<Typography
                                fontWeight={800}>{form.tradeName || "Sua marca"}</Typography></Stack><Box><Typography
                                variant="body2" sx={{color: "rgba(255,255,255,.72)"}}>Painel
                                operacional</Typography><Typography sx={{fontSize: 23, fontWeight: 800}}>Sua operação,
                                sua marca.</Typography></Box></Box>
                            <CardContent sx={{p: 3}}><Grid container
                                                           spacing={1.5}>{[["Ordens abertas", "18"], ["Clientes ativos", "124"]].map(([label, value], index) =>
                                <Grid key={label} size={{xs: 6}}><Box sx={{
                                    p: 2,
                                    borderRadius: `${Math.max(6, form.borderRadius - 3)}px`,
                                    bgcolor: "#F8FAFC",
                                    border: "1px solid #EEF1F6"
                                }}><Typography variant="caption" color="text.secondary">{label}</Typography><Typography
                                    sx={{
                                        fontSize: 24,
                                        fontWeight: 800,
                                        color: index ? form.secondaryColor : form.primaryColor
                                    }}>{value}</Typography></Box></Grid>)}</Grid><Button variant="contained" fullWidth
                                                                                         sx={{
                                                                                             mt: 2.5,
                                                                                             bgcolor: form.primaryColor,
                                                                                             borderRadius: `${form.borderRadius - 2}px`
                                                                                         }}>Nova ordem de
                                serviço</Button></CardContent>
                        </Card>
                        <Box sx={{mt: 2, p: 2.5, borderRadius: 3, bgcolor: "#EFF4FF"}}><Typography variant="body2"
                                                                                                   fontWeight={750}>Identidade
                            do cadastro</Typography><Typography variant="body2" color="text.secondary"
                                                                mt={0.5}>{remoteBrand ? `${remoteBrand.tradeName} • ${remoteBrand.primaryColor || "cor padrão"}` : "Nenhuma identidade remota carregada."}</Typography></Box>
                    </Box>
                </Grid>
            </Grid>
        </>
    );
}
