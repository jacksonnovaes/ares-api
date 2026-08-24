package br.com.ares.asset.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assets")
class AssetJpaEntity {
    @Id
    UUID id;
    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;
    @Column(name = "customer_id", nullable = false)
    UUID customerId;
    @Column(nullable = false, length = 50)
    String type;
    @Column(nullable = false)
    String name;
    String brand;
    String model;
    @Column(name = "serial_number")
    String serialNumber;
    @Column(name = "attributes_json", nullable = false, columnDefinition = "text")
    String attributesJson;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    protected AssetJpaEntity() {
    }
}
