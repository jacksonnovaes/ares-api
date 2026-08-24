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
        boolean requireAssets,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    public Tenant withRequireAssets(boolean value, Instant at) {
        return new Tenant(id, legalName, tradeName, slug, document, status, logoUrl, primaryColor, value,
                createdAt, at);
    }
}
