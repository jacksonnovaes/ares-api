package br.com.ares.audit.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
class AuditEventJpaEntity {
    @Id UUID id;
    @Column(name = "tenant_id") UUID tenantId;
    @Column(name = "actor_id") UUID actorId;
    @Column(nullable = false) String action;
    @Column(name = "resource_type", nullable = false) String resourceType;
    @Column(name = "resource_id") String resourceId;
    @Column(name = "details_json", nullable = false, columnDefinition = "text") String detailsJson;
    @Column(name = "occurred_at", nullable = false) Instant occurredAt;

    protected AuditEventJpaEntity() {
    }
}
