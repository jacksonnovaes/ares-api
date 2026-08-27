package br.com.ares.serviceorder.adapter.out.persistence;

import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

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
    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false, length = 30)
    QuoteCalculationMethod calculationMethod;
    @Column(name = "width_meters", precision = 12, scale = 3)
    BigDecimal widthMeters;
    @Column(name = "length_meters", precision = 12, scale = 3)
    BigDecimal lengthMeters;
    @Column(name = "height_meters", precision = 12, scale = 3)
    BigDecimal heightMeters;

    protected ServiceOrderLineJpaEmbeddable() {
    }
}
