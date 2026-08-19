import { Chip } from "@mui/material";
import type { ChipProps } from "@mui/material";

const labels: Record<string, string> = {
  ACTIVE: "Ativo", INACTIVE: "Inativo", PENDING: "Pendente", BLOCKED: "Bloqueado",
  OPEN: "Aberta", IN_DIAGNOSIS: "Em diagnóstico", WAITING_APPROVAL: "Aguardando aprovação",
  IN_PROGRESS: "Em andamento", COMPLETED: "Concluída", CANCELLED: "Cancelada",
  LOW: "Baixa", NORMAL: "Normal", HIGH: "Alta", URGENT: "Urgente",
};

const colors: Record<string, ChipProps["color"]> = {
  ACTIVE: "success", COMPLETED: "success", IN_PROGRESS: "primary",
  OPEN: "info", IN_DIAGNOSIS: "secondary", WAITING_APPROVAL: "warning", PENDING: "warning",
  CANCELLED: "default", INACTIVE: "default", BLOCKED: "error", URGENT: "error", HIGH: "warning",
};

export function StatusChip({ value }: { value: string }) {
  return <Chip label={labels[value] ?? value} color={colors[value] ?? "default"} size="small" variant={value === "CANCELLED" || value === "INACTIVE" ? "outlined" : "filled"} />;
}

export const enumLabel = (value: string) => labels[value] ?? value.replaceAll("_", " ").toLowerCase();
