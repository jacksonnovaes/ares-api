"use client";

import { AuthShell } from "@/components/auth/auth-shell";
import { apiRequest, errorMessage } from "@/lib/api";
import CheckRoundedIcon from "@mui/icons-material/CheckRounded";
import { Alert, Button, Link as MuiLink, Stack, TextField } from "@mui/material";
import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";

export default function ResetPasswordPage() {
  const [token, setToken] = useState("");
  const [password, setPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  useEffect(() => setToken(new URLSearchParams(window.location.search).get("token") ?? ""), []);
  async function submit(event: FormEvent) {
    event.preventDefault(); setError("");
    if (password !== confirmation) return setError("A confirmação da senha não confere.");
    setLoading(true);
    try { await apiRequest<void>("/auth/reset-password", { method: "POST", body: { token, newPassword: password, passwordConfirmation: confirmation } }); setMessage("Senha redefinida. Você já pode entrar com a nova credencial."); }
    catch (err) { setError(errorMessage(err)); }
    finally { setLoading(false); }
  }
  return <AuthShell title="Defina uma nova senha" subtitle="Use o token recebido e escolha uma senha segura para sua conta."><Stack component="form" onSubmit={submit} spacing={2.25}>
    {message && <Alert severity="success">{message}</Alert>}{error && <Alert severity="error">{error}</Alert>}
    <TextField label="Token de recuperação" value={token} onChange={(e) => setToken(e.target.value)} required fullWidth />
    <TextField label="Nova senha" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required fullWidth />
    <TextField label="Confirmar nova senha" type="password" value={confirmation} onChange={(e) => setConfirmation(e.target.value)} required fullWidth />
    <Button type="submit" variant="contained" size="large" disabled={loading || Boolean(message)} startIcon={<CheckRoundedIcon />}>{loading ? "Redefinindo..." : "Redefinir senha"}</Button>
    {message && <MuiLink component={Link} href="/" fontWeight={700} textAlign="center">Voltar para o login</MuiLink>}
  </Stack></AuthShell>;
}
