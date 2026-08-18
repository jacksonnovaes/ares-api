package br.com.ares.serviceorder.domain.model;

public enum ServiceOrderStatus {
    OPEN,
    IN_DIAGNOSIS,
    WAITING_APPROVAL,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
