package br.com.ares.serviceorder.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.UUID;

@Embeddable
class ServiceOrderLineJpaEmbeddable {
    @Column(name = "service_id")
    UUID serviceId;
    @Column(nullable = false, length = 500)
    String description;
    @Column(nullable = false, precision = 12, scale = 3)
    BigDecimal quantity;
    @Column(nullable = false, length = 20)
    String unit;
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    BigDecimal unitPrice;

    protected ServiceOrderLineJpaEmbeddable() {
    }
}
