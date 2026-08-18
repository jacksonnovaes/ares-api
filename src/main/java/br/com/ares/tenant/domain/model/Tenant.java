package br.com.ares.tenant.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Tenant(
        UUID id,
        String legalName,
        String tradeName,
        String slug,
        String document,
        TenantStatus status,
        String logoUrl,
        String primaryColor,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }
}
