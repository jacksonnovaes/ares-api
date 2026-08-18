package br.com.ares.identity.adapter.out.persistence;

import br.com.ares.identity.domain.model.Permission;
import br.com.ares.identity.domain.model.Role;
import br.com.ares.identity.domain.model.UserStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
class UserJpaEntity {
    @Id UUID id;
    @Column(name = "tenant_id", nullable = false) UUID tenantId;
    @Column(name = "customer_id") UUID customerId;
    @Column(nullable = false) String name;
    @Column(nullable = false) String email;
    @Column(name = "password_hash", nullable = false) String passwordHash;
    String phone;
    @Column(name = "job_title") String jobTitle;
    @Enumerated(EnumType.STRING) @Column(nullable = false) UserStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    Set<Role> roles = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission", nullable = false)
    @Enumerated(EnumType.STRING)
    Set<Permission> permissions = new LinkedHashSet<>();

    @Column(name = "last_login_at") Instant lastLoginAt;
    @Column(name = "password_changed_at") Instant passwordChangedAt;
    @Column(name = "created_at", nullable = false) Instant createdAt;
    @Column(name = "updated_at", nullable = false) Instant updatedAt;

    protected UserJpaEntity() {
    }
}
