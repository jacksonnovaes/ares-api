package br.com.ares.servicecatalog.adapter.out.persistence;

import br.com.ares.servicecatalog.domain.model.CatalogServiceType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="catalog_services")
class CatalogServiceJpaEntity {
    @Id UUID id; @Column(name="tenant_id",nullable=false) UUID tenantId;
    @Column(nullable=false) String name; @Column(columnDefinition="text") String description;
    @Column(name="base_price",nullable=false) BigDecimal basePrice;
    @Column(name="estimated_minutes") Integer estimatedMinutes;
    @Enumerated(EnumType.STRING) @Column(name="service_type",nullable=false) CatalogServiceType type;
    @Column(nullable=false) boolean active;
    @Column(name="created_at",nullable=false) Instant createdAt;
    @Column(name="updated_at",nullable=false) Instant updatedAt;
    protected CatalogServiceJpaEntity() {}
}
