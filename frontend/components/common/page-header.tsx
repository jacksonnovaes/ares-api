import { Box, Button, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

interface PageHeaderProps {
  eyebrow?: string;
  title: string;
  description: string;
  actionLabel?: string;
  actionIcon?: ReactNode;
  onAction?: () => void;
  children?: ReactNode;
}

export function PageHeader({ eyebrow, title, description, actionLabel, actionIcon, onAction, children }: PageHeaderProps) {
  return (
    <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" alignItems={{ sm: "flex-end" }} gap={2.5} mb={3}>
      <Box>
        {eyebrow && <Typography variant="overline" color="primary.main" fontWeight={800} letterSpacing={1.2}>{eyebrow}</Typography>}
        <Typography component="h1" variant="h1">{title}</Typography>
        <Typography color="text.secondary" mt={0.75}>{description}</Typography>
      </Box>
      {children ?? (actionLabel && <Button variant="contained" startIcon={actionIcon} onClick={onAction}>{actionLabel}</Button>)}
    </Stack>
  );
}
