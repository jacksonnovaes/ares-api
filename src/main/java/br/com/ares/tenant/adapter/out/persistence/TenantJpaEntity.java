package br.com.ares.tenant.adapter.out.persistence;

import br.com.ares.tenant.domain.model.TenantStatus;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import br.com.ares.tenant.domain.model.PublicServiceSource;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_billing_cycle", nullable = false, length = 20)
    SubscriptionBillingCycle subscriptionBillingCycle;
    @Column(name = "additional_user_seats", nullable = false) int additionalUserSeats;
    @Column(name = "subscription_active", nullable = false) boolean subscriptionActive;
    @Column(name = "subscription_paid_until") Instant subscriptionPaidUntil;
    @Column(name = "subscription_price", nullable = false, precision = 15, scale = 2)
    BigDecimal subscriptionPrice;
    @Column(name = "coupon_code", length = 40) String couponCode;
    @Column(name = "coupon_discount_percentage", nullable = false, precision = 5, scale = 2)
    BigDecimal couponDiscountPercentage;
    @Enumerated(EnumType.STRING)
    @Column(name = "quote_calculation_method", nullable = false, length = 30)
    QuoteCalculationMethod quoteCalculationMethod;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tenant_quote_calculation_methods", joinColumns = @JoinColumn(name = "tenant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false, length = 30)
    Set<QuoteCalculationMethod> enabledQuoteCalculationMethods = new LinkedHashSet<>();
    @Column(name = "default_square_meter_price", precision = 15, scale = 2)
    BigDecimal defaultSquareMeterPrice;
    @Column(name = "default_cubic_meter_price", precision = 15, scale = 2)
    BigDecimal defaultCubicMeterPrice;
    @Column(name = "public_page_enabled", nullable = false)
    boolean publicPageEnabled;
    @Column(name = "public_headline", length = 180)
    String publicHeadline;
    @Column(name = "public_description", length = 1200)
    String publicDescription;
    @Column(name = "public_whatsapp", length = 13)
    String publicWhatsapp;
    @Column(name = "public_email", length = 254)
    String publicEmail;
    @Column(name = "public_city", length = 120)
    String publicCity;
    @Column(name = "public_service_area", length = 180)
    String publicServiceArea;
    @Column(name = "public_show_prices", nullable = false)
    boolean publicShowPrices;
    @Enumerated(EnumType.STRING)
    @Column(name = "public_service_source", nullable = false, length = 20)
    PublicServiceSource publicServiceSource;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tenant_public_profile_services", joinColumns = @JoinColumn(name = "tenant_id"))
    @OrderColumn(name = "display_order")
    List<PublicProfileManualServiceJpaEmbeddable> publicManualServices = new ArrayList<>();
    @Column(name = "public_accent_color", nullable = false, length = 7)
    String publicAccentColor;
    @Column(name = "public_background_color", nullable = false, length = 7)
    String publicBackgroundColor;
    @Column(name = "public_text_color", nullable = false, length = 7)
    String publicTextColor;
    @Column(name = "public_profile_image_path", length = 500)
    String publicProfileImagePath;
    @Column(name = "public_logo_path", length = 500)
    String publicLogoPath;
    @Column(name = "public_background_image_path", length = 500)
    String publicBackgroundImagePath;
    @Column(name = "public_show_logo", nullable = false)
    boolean publicShowLogo;
    @Column(name = "public_background_overlay_percentage", nullable = false)
    int publicBackgroundOverlayPercentage;
    @Column(name = "created_at", nullable = false) Instant createdAt;
    @Column(name = "updated_at", nullable = false) Instant updatedAt;

    protected TenantJpaEntity() {
    }
}
