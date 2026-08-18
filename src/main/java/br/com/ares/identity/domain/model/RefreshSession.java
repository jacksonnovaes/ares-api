package br.com.ares.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

public record RefreshSession(
        UUID id,
        UUID familyId,
        UUID userId,
        UUID tenantId,
        String tokenHash,
        Instant expiresAt,
        Instant revokedAt,
        UUID replacedBy,
        Instant createdAt
) {
    public boolean isActiveAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public RefreshSession revoke(Instant at, UUID replacement) {
        return new RefreshSession(id, familyId, userId, tenantId, tokenHash, expiresAt,
                at, replacement, createdAt);
    }
}
