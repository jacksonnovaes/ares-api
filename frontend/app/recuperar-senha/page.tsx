"use client";

import { AuthShell } from "@/components/auth/auth-shell";
import { apiRequest, errorMessage } from "@/lib/api";
import ArrowBackRoundedIcon from "@mui/icons-material/ArrowBackRounded";
import SendRoundedIcon from "@mui/icons-material/SendRounded";
import { Alert, Button, Link as MuiLink, Stack, TextField } from "@mui/material";
import Link from "next/link";
import { FormEvent, useState } from "react";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  async function submit(event: FormEvent) {
    event.preventDefault(); setLoading(true); setError("");
    try {
      const result = await apiRequest<{ message: string }>("/auth/forgot-password", { method: "POST", body: { email } });
      setMessage(result.message);
    } catch (err) { setError(errorMessage(err)); }
    finally { setLoading(false); }
  }
  return (
    <AuthShell title="Recupere seu acesso" subtitle="Informe o e-mail usado no cadastro para receber as instruções.">
      <Stack component="form" onSubmit={submit} spacing={2.25}>
        {message && <Alert severity="success">{message}</Alert>}
        {error && <Alert severity="error">{error}</Alert>}
        <TextField label="E-mail" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required fullWidth autoFocus />
        <Button type="submit" variant="contained" size="large" disabled={loading} startIcon={<SendRoundedIcon />}>{loading ? "Enviando..." : "Enviar instruções"}</Button>
        <MuiLink component={Link} href="/" underline="hover" sx={{ display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 1, fontWeight: 700 }}><ArrowBackRoundedIcon fontSize="small" />Voltar para o login</MuiLink>
      </Stack>
    </AuthShell>
  );
}
