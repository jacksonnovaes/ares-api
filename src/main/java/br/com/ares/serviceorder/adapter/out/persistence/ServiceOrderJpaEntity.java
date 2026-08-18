package br.com.ares.serviceorder.adapter.out.persistence;

import br.com.ares.serviceorder.domain.model.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity @Table(name="service_orders")
class ServiceOrderJpaEntity {
    @Id UUID id;@Column(name="tenant_id",nullable=false)UUID tenantId;
    @Column(name="customer_id",nullable=false)UUID customerId;@Column(name="asset_id",nullable=false)UUID assetId;
    @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="service_order_services",
            joinColumns=@JoinColumn(name="service_order_id")) @Column(name="service_id",nullable=false)
    Set<UUID> serviceIds=new LinkedHashSet<>();
    @Column(nullable=false)String title;@Column(columnDefinition="text")String description;
    @Enumerated(EnumType.STRING)@Column(nullable=false)ServiceOrderStatus status;
    @Enumerated(EnumType.STRING)@Column(nullable=false)ServiceOrderPriority priority;
    @Column(name="estimated_value")BigDecimal estimatedValue;@Column(name="final_value")BigDecimal finalValue;
    @Column(name="assigned_technician_id")UUID assignedTechnicianId;
    @Column(name="opened_at",nullable=false)Instant openedAt;@Column(name="due_at")Instant dueAt;
    @Column(name="completed_at")Instant completedAt;@Column(name="created_at",nullable=false)Instant createdAt;
    @Column(name="updated_at",nullable=false)Instant updatedAt;protected ServiceOrderJpaEntity(){}
}
