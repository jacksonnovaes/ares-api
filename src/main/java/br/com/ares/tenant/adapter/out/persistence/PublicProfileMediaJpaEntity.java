package br.com.ares.tenant.adapter.out.persistence;

import br.com.ares.tenant.domain.model.PublicProfileMediaKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_public_profile_media")
class PublicProfileMediaJpaEntity {
    @Id UUID id;
    @Column(name = "tenant_id", nullable = false) UUID tenantId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) PublicProfileMediaKind kind;
    @Column(nullable = false, length = 100) String filename;
    @Column(name = "content_type", nullable = false, length = 50) String contentType;
    @Column(nullable = false, columnDefinition = "bytea") byte[] content;
    @Column(name = "size_bytes", nullable = false) int sizeBytes;
    @Column(name = "created_at", nullable = false) Instant createdAt;
    @Column(name = "updated_at", nullable = false) Instant updatedAt;

    protected PublicProfileMediaJpaEntity() {
    }
}
