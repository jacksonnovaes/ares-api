package br.com.ares.identity.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetJpaEntity {
    @Id
    UUID id;
    @Column(name = "user_id", nullable = false)
    UUID userId;
    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;
    @Column(name = "token_hash", nullable = false)
    String tokenHash;
    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;
    @Column(name = "used_at")
    Instant usedAt;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    protected PasswordResetJpaEntity() {
    }
}
