package br.com.ares.identity.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
class RefreshSessionJpaEntity {
    @Id
    UUID id;
    @Column(name = "family_id", nullable = false)
    UUID familyId;
    @Column(name = "user_id", nullable = false)
    UUID userId;
    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;
    @Column(name = "token_hash", nullable = false)
    String tokenHash;
    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;
    @Column(name = "revoked_at")
    Instant revokedAt;
    @Column(name = "replaced_by")
    UUID replacedBy;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    protected RefreshSessionJpaEntity() {
    }
}
