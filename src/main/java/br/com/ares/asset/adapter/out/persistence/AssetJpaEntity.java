package br.com.ares.asset.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assets")
@Getter
@Setter
public class AssetJpaEntity {
    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    @Column(nullable = false, length = 50)
    private String type;
    @Column(nullable = false)
    private String name;
    private String brand;
    private String model;
    @Column(name = "serial_number")
    private String serialNumber;
    @Column(name = "attributes_json", nullable = false, columnDefinition = "text")
    private String attributesJson;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AssetJpaEntity() {
    }

}
