package br.com.ares.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PasswordReset(
        UUID id,
        UUID userId,
        UUID tenantId,
        String tokenHash,
        Instant expiresAt,
        Instant usedAt,
        Instant createdAt
) {
    public boolean isUsableAt(Instant now) {
        return usedAt == null && expiresAt.isAfter(now);
    }

    public PasswordReset markUsed(Instant at) {
        return new PasswordReset(id, userId, tenantId, tokenHash, expiresAt, at, createdAt);
    }
}
