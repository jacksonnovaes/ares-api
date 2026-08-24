package br.com.ares.tenant.adapter.out.persistence;

import br.com.ares.tenant.domain.model.TenantStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
class TenantJpaEntity {
    @Id UUID id;
    @Column(name = "legal_name", nullable = false) String legalName;
    @Column(name = "trade_name", nullable = false) String tradeName;
    @Column(nullable = false) String slug;
    @Column(nullable = false) String document;
    @Enumerated(EnumType.STRING) @Column(nullable = false) TenantStatus status;
    @Column(name = "logo_url") String logoUrl;
    @Column(name = "primary_color") String primaryColor;
    @Column(name = "require_assets", nullable = false) boolean requireAssets;
    @Column(name = "created_at", nullable = false) Instant createdAt;
    @Column(name = "updated_at", nullable = false) Instant updatedAt;

    protected TenantJpaEntity() {
    }
}
