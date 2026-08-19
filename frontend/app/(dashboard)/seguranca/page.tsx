"use client";

import { PageHeader } from "@/components/common/page-header";
import { apiRequest, errorMessage } from "@/lib/api";
import CheckRoundedIcon from "@mui/icons-material/CheckRounded";
import LockResetRoundedIcon from "@mui/icons-material/LockResetRounded";
import { Alert, Box, Button, Card, CardContent, Stack, TextField, Typography } from "@mui/material";
import { FormEvent, useState } from "react";

export default function SecurityPage() {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  async function submit(event: FormEvent) {
    event.preventDefault(); setError(""); setMessage("");
    if (newPassword !== confirmation) return setError("A confirmação da nova senha não confere.");
    setLoading(true);
    try {
      await apiRequest<void>("/auth/change-password", { method: "POST", body: { currentPassword, newPassword, passwordConfirmation: confirmation } });
      setCurrentPassword(""); setNewPassword(""); setConfirmation(""); setMessage("Senha alterada com sucesso.");
    } catch (err) { setError(errorMessage(err)); }
    finally { setLoading(false); }
  }
  return <><PageHeader eyebrow="Conta" title="Segurança" description="Atualize sua senha de acesso ao ambiente." /><Card sx={{ maxWidth: 680 }}><CardContent sx={{ p: { xs: 2.5, sm: 3.5 } }}><Stack direction="row" alignItems="center" spacing={1.5} mb={3}><Box sx={{ width: 44, height: 44, display: "grid", placeItems: "center", borderRadius: 2.25, bgcolor: "primary.main", color: "white" }}><LockResetRoundedIcon /></Box><Box><Typography variant="h3">Alterar senha</Typography><Typography variant="body2" color="text.secondary">Use uma senha exclusiva e difícil de adivinhar.</Typography></Box></Stack><Stack component="form" onSubmit={submit} spacing={2.25}>{message && <Alert severity="success" icon={<CheckRoundedIcon />}>{message}</Alert>}{error && <Alert severity="error">{error}</Alert>}<TextField label="Senha atual" type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required fullWidth /><TextField label="Nova senha" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required fullWidth /><TextField label="Confirmar nova senha" type="password" value={confirmation} onChange={(e) => setConfirmation(e.target.value)} required fullWidth /><Button type="submit" variant="contained" disabled={loading} sx={{ alignSelf: "flex-start" }}>{loading ? "Alterando..." : "Alterar senha"}</Button></Stack></CardContent></Card></>;
}
