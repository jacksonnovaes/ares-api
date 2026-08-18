package br.com.ares.customer.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Customer(UUID id, UUID tenantId, CustomerType type, String name, String document,
                       String email, String phone, String notes, CustomerStatus status,
                       Instant createdAt, Instant updatedAt) {
}
