package br.com.ares.customer.adapter.out.persistence;

import br.com.ares.customer.domain.model.CustomerStatus;
import br.com.ares.customer.domain.model.CustomerType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
class CustomerJpaEntity {
    @Id
    UUID id;
    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CustomerType type;
    @Column(nullable = false)
    String name;
    String document;
    String email;
    String phone;
    @Column(columnDefinition = "text")
    String notes;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CustomerStatus status;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    protected CustomerJpaEntity() {
    }
}
