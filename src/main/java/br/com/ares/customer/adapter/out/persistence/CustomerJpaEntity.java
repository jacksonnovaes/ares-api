package br.com.ares.customer.adapter.out.persistence;

import br.com.ares.customer.domain.model.CustomerStatus;
import br.com.ares.customer.domain.model.CustomerType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
public class CustomerJpaEntity {
    @Id
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType type;
    @Column(nullable = false)
    private String name;
    private String document;
    private String email;
    private String phone;
    @Column(length = 500)
    private String address;
    @Column(columnDefinition = "text")
    private String notes;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CustomerJpaEntity() {
    }
}
