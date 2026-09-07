package br.com.ares.tenant.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
class PublicProfileManualServiceJpaEmbeddable {
    @Column(nullable = false, length = 160)
    String name;
    @Column(length = 1000)
    String description;
    @Column(name = "base_price", precision = 15, scale = 2)
    BigDecimal basePrice;

    protected PublicProfileManualServiceJpaEmbeddable() {
    }
}

