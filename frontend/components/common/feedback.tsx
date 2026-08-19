import InboxOutlinedIcon from "@mui/icons-material/InboxOutlined";
import RefreshRoundedIcon from "@mui/icons-material/RefreshRounded";
import { Alert, Box, Button, CircularProgress, TableCell, TableRow, Typography } from "@mui/material";

export function PageLoading({ label = "Carregando informações..." }: { label?: string }) {
  return (
    <Box sx={{ minHeight: 280, display: "grid", placeItems: "center", textAlign: "center" }}>
      <Box><CircularProgress size={32} /><Typography color="text.secondary" mt={1.5}>{label}</Typography></Box>
    </Box>
  );
}

export function TableLoading({ colSpan }: { colSpan: number }) {
  return <TableRow><TableCell colSpan={colSpan} align="center" sx={{ py: 8 }}><CircularProgress size={28} /></TableCell></TableRow>;
}

export function TableEmpty({ colSpan, message = "Nenhum registro encontrado." }: { colSpan: number; message?: string }) {
  return (
    <TableRow><TableCell colSpan={colSpan} align="center" sx={{ py: 7 }}>
      <InboxOutlinedIcon sx={{ fontSize: 36, color: "text.disabled" }} />
      <Typography color="text.secondary" mt={1}>{message}</Typography>
    </TableCell></TableRow>
  );
}

export function ErrorAlert({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return <Alert severity="error" action={onRetry && <Button color="inherit" size="small" startIcon={<RefreshRoundedIcon />} onClick={onRetry}>Tentar novamente</Button>}>{message}</Alert>;
}
