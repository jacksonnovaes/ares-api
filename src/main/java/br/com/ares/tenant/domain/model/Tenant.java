package br.com.ares.tenant.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Tenant(
        UUID id,
        String legalName,
        String tradeName,
        String slug,
        String document,
        TenantStatus status,
        String logoUrl,
        String primaryColor,
        boolean requireAssets,
        SubscriptionPlan subscriptionPlan,
        boolean subscriptionActive,
        Instant subscriptionPaidUntil,
        BigDecimal subscriptionMonthlyPrice,
        String couponCode,
        BigDecimal couponDiscountPercentage,
        QuoteCalculationMethod quoteCalculationMethod,
        BigDecimal defaultSquareMeterPrice,
        BigDecimal defaultCubicMeterPrice,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean isActive() {
        return status == TenantStatus.ACTIVE && subscriptionActive;
    }

    public Tenant withRequireAssets(boolean value, Instant at) {
        return new Tenant(id, legalName, tradeName, slug, document, status, logoUrl, primaryColor, value,
                subscriptionPlan, subscriptionActive, subscriptionPaidUntil, subscriptionMonthlyPrice,
                couponCode, couponDiscountPercentage, quoteCalculationMethod, defaultSquareMeterPrice,
                defaultCubicMeterPrice, createdAt, at);
    }

    public Tenant withCompanySettings(boolean assetsRequired, QuoteCalculationMethod calculationMethod,
                                      BigDecimal squareMeterPrice, BigDecimal cubicMeterPrice, Instant at) {
        return new Tenant(id, legalName, tradeName, slug, document, status, logoUrl, primaryColor, assetsRequired,
                subscriptionPlan, subscriptionActive, subscriptionPaidUntil, subscriptionMonthlyPrice,
                couponCode, couponDiscountPercentage, calculationMethod, squareMeterPrice, cubicMeterPrice,
                createdAt, at);
    }
}
