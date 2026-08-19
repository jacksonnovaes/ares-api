"use client";

import { AuthShell } from "@/components/auth/auth-shell";
import { apiRequest, errorMessage } from "@/lib/api";
import CheckRoundedIcon from "@mui/icons-material/CheckRounded";
import { Alert, Box, Button, Grid, InputAdornment, Stack, TextField, Typography } from "@mui/material";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";

const initial = {
  legalName: "", tradeName: "", slug: "", document: "", logoUrl: "", primaryColor: "#2457E6",
  adminName: "", email: "", password: "", confirmation: "",
};

export default function RegisterPage() {
  const router = useRouter();
  const [form, setForm] = useState(initial);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const set = (field: keyof typeof form, value: string) => setForm((current) => ({ ...current, [field]: value }));

  function setTradeName(value: string) {
    setForm((current) => ({
      ...current,
      tradeName: value,
      slug: current.slug || value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, ""),
    }));
  }

  async function submit(event: FormEvent) {
    event.preventDefault(); setError("");
    if (form.password !== form.confirmation) return setError("A confirmação da senha não confere.");
    setSubmitting(true);
    try {
      await apiRequest("/tenants/register", { method: "POST", body: {
        legalName: form.legalName, tradeName: form.tradeName, slug: form.slug, document: form.document,
        logoUrl: form.logoUrl || null, primaryColor: form.primaryColor,
        admin: { name: form.adminName, email: form.email, password: form.password, passwordConfirmation: form.confirmation },
      } });
      localStorage.setItem("ares.lastTenantSlug", form.slug);
      router.push("/?cadastro=sucesso");
    } catch (err) { setError(errorMessage(err)); }
    finally { setSubmitting(false); }
  }

  return (
    <AuthShell title="Crie seu espaço" subtitle="Cadastre a empresa e personalize a identidade que sua equipe verá.">
      <Box component="form" onSubmit={submit}>
        <Stack spacing={3}>
          {error && <Alert severity="error">{error}</Alert>}
          <Box><Typography variant="h3" mb={2}>Dados da empresa</Typography>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12 }}><TextField label="Razão social" required fullWidth value={form.legalName} onChange={(e) => set("legalName", e.target.value)} /></Grid>
              <Grid size={{ xs: 12, sm: 7 }}><TextField label="Nome da marca" required fullWidth value={form.tradeName} onChange={(e) => setTradeName(e.target.value)} /></Grid>
              <Grid size={{ xs: 12, sm: 5 }}><TextField label="Documento" required fullWidth value={form.document} onChange={(e) => set("document", e.target.value)} /></Grid>
              <Grid size={{ xs: 12 }}><TextField label="Identificador da empresa" helperText="Apenas letras minúsculas, números e hífens." required fullWidth value={form.slug} onChange={(e) => set("slug", e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, ""))} /></Grid>
            </Grid>
          </Box>
          <Box><Typography variant="h3" mb={2}>Identidade visual</Typography>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 8 }}><TextField label="URL do logo" fullWidth value={form.logoUrl} onChange={(e) => set("logoUrl", e.target.value)} /></Grid>
              <Grid size={{ xs: 12, sm: 4 }}><TextField label="Cor principal" fullWidth value={form.primaryColor} onChange={(e) => set("primaryColor", e.target.value)} slotProps={{ input: { startAdornment: <InputAdornment position="start"><Box component="input" type="color" value={form.primaryColor} onChange={(e) => set("primaryColor", e.target.value)} sx={{ width: 26, height: 26, p: 0, border: 0, bgcolor: "transparent", cursor: "pointer" }} /></InputAdornment> } }} /></Grid>
            </Grid>
          </Box>
          <Box><Typography variant="h3" mb={2}>Administrador</Typography>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12 }}><TextField label="Nome completo" required fullWidth value={form.adminName} onChange={(e) => set("adminName", e.target.value)} /></Grid>
              <Grid size={{ xs: 12 }}><TextField label="E-mail" type="email" required fullWidth value={form.email} onChange={(e) => set("email", e.target.value)} /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><TextField label="Senha" type="password" required fullWidth value={form.password} onChange={(e) => set("password", e.target.value)} /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><TextField label="Confirmar senha" type="password" required fullWidth value={form.confirmation} onChange={(e) => set("confirmation", e.target.value)} /></Grid>
            </Grid>
          </Box>
          <Button type="submit" variant="contained" size="large" disabled={submitting} startIcon={<CheckRoundedIcon />}>{submitting ? "Criando espaço..." : "Criar minha empresa"}</Button>
        </Stack>
      </Box>
    </AuthShell>
  );
}
