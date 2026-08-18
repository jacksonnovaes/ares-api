package br.com.ares.identity.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record User(
        UUID id,
        UUID tenantId,
        UUID customerId,
        String name,
        String email,
        String passwordHash,
        String phone,
        String jobTitle,
        UserStatus status,
        Set<Role> roles,
        Set<Permission> permissions,
        Instant lastLoginAt,
        Instant passwordChangedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean canAuthenticate() {
        return status == UserStatus.ACTIVE;
    }

    public User withLastLogin(Instant value) {
        return new User(id, tenantId, customerId, name, email, passwordHash, phone, jobTitle, status,
                roles, permissions, value, passwordChangedAt, createdAt, value);
    }

    public User withPassword(String hash, Instant value) {
        return new User(id, tenantId, customerId, name, email, hash, phone, jobTitle, status,
                roles, permissions, lastLoginAt, value, createdAt, value);
    }

    public User withStatus(UserStatus value, Instant updatedAt) {
        return new User(id, tenantId, customerId, name, email, passwordHash, phone, jobTitle, value,
                roles, permissions, lastLoginAt, passwordChangedAt, createdAt, updatedAt);
    }
}
