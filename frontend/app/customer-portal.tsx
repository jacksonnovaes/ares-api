"use client";

import {
    FormEvent,
    KeyboardEvent,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from "react";

const API_URL = (
    process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"
).replace(/\/$/, "");
const SESSION_KEY = "ares.customer.session";

type OrderStatus =
    | "OPEN"
    | "IN_DIAGNOSIS"
    | "WAITING_APPROVAL"
    | "IN_PROGRESS"
    | "COMPLETED"
    | "CANCELLED";

type OrderPriority = "LOW" | "NORMAL" | "HIGH" | "URGENT";

type CustomerOrder = {
    id: string;
    assetId: string;
    serviceIds: string[];
    title: string;
    description?: string;
    status: OrderStatus;
    priority: OrderPriority;
    estimatedValue?: number;
    finalValue?: number;
    openedAt: string;
    dueAt?: string;
    completedAt?: string;
    createdAt: string;
    updatedAt: string;
};

type Session = {
    accessToken: string;
    refreshToken: string;
    expiresIn: number;
    user: {
        id: string;
        name: string;
        tenantId: string;
        roles: string[];
    };
};

type Problem = {
    detail?: string;
    message?: string;
};

const statusLabels: Record<OrderStatus, string> = {
    OPEN: "Aberta",
    IN_DIAGNOSIS: "Em diagnóstico",
    WAITING_APPROVAL: "Aguardando aprovação",
    IN_PROGRESS: "Em andamento",
    COMPLETED: "Concluída",
    CANCELLED: "Cancelada",
};

const priorityLabels: Record<OrderPriority, string> = {
    LOW: "Baixa",
    NORMAL: "Normal",
    HIGH: "Alta",
    URGENT: "Urgente",
};

const filters = [
    { value: "ALL", label: "Todas" },
    { value: "ACTIVE", label: "Em andamento" },
    { value: "WAITING_APPROVAL", label: "Aguardando" },
    { value: "COMPLETED", label: "Concluídas" },
] as const;

type FilterValue = (typeof filters)[number]["value"];

function loadStoredSession(): Session | null {
    if (typeof window === "undefined") return null;
    try {
        const value = window.sessionStorage.getItem(SESSION_KEY);
        return value ? (JSON.parse(value) as Session) : null;
    } catch {
        window.sessionStorage.removeItem(SESSION_KEY);
        return null;
    }
}