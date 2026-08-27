package br.com.ares.tenant.adapter.out.persistence;

import br.com.ares.tenant.domain.model.TenantStatus;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
class TenantJpaEntity {
    @Id UUID id;
    @Column(name = "legal_name", nullable = false) String legalName;
    @Column(name = "trade_name", nullable = false) String tradeName;
    @Column(nullable = false) String slug;
    @Column(nullable = false) String document;
    @Enumerated(EnumType.STRING) @Column(nullable = false) TenantStatus status;
    @Column(name = "logo_url") String logoUrl;
    @Column(name = "primary_color") String primaryColor;
    @Column(name = "require_assets", nullable = false) boolean requireAssets;
    @Enumerated(EnumType.STRING) @Column(name = "subscription_plan", nullable = false) SubscriptionPlan subscriptionPlan;
    @Column(name = "subscription_active", nullable = false) boolean subscriptionActive;
    @Column(name = "subscription_paid_until") Instant subscriptionPaidUntil;
    @Column(name = "subscription_monthly_price", nullable = false, precision = 15, scale = 2)
    BigDecimal subscriptionMonthlyPrice;
    @Column(name = "coupon_code", length = 40) String couponCode;
    @Column(name = "coupon_discount_percentage", nullable = false, precision = 5, scale = 2)
    BigDecimal couponDiscountPercentage;
    @Enumerated(EnumType.STRING)
    @Column(name = "quote_calculation_method", nullable = false, length = 30)
    QuoteCalculationMethod quoteCalculationMethod;
    @Column(name = "default_square_meter_price", precision = 15, scale = 2)
    BigDecimal defaultSquareMeterPrice;
    @Column(name = "default_cubic_meter_price", precision = 15, scale = 2)
    BigDecimal defaultCubicMeterPrice;
    @Column(name = "created_at", nullable = false) Instant createdAt;
    @Column(name = "updated_at", nullable = false) Instant updatedAt;

    protected TenantJpaEntity() {
    }
}
