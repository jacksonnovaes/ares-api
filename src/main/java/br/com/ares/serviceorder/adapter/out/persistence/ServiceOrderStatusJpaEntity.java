package br.com.ares.serviceorder.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_order_statuses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_order_statuses_tenant_code", columnNames = {"tenant_id", "code"}),
        @UniqueConstraint(name = "uk_order_statuses_tenant_name", columnNames = {"tenant_id", "name"})
})
class ServiceOrderStatusJpaEntity {
    @Id UUID id;
    @Column(name = "tenant_id", nullable = false) UUID tenantId;
    @Column(nullable = false, length = 50) String code;
    @Column(nullable = false, length = 100) String name;
    @Column(name = "system_default", nullable = false) boolean systemDefault;
    @Column(nullable = false) boolean active;
    @Column(name = "display_order", nullable = false) int displayOrder;
    @Column(name = "created_at", nullable = false) Instant createdAt;
    @Column(name = "updated_at", nullable = false) Instant updatedAt;

    protected ServiceOrderStatusJpaEntity() {
    }
}
