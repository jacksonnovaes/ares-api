package br.com.ares.tenant.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
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
        SubscriptionBillingCycle subscriptionBillingCycle,
        int additionalUserSeats,
        boolean subscriptionActive,
        Instant subscriptionPaidUntil,
        BigDecimal subscriptionPrice,
        String couponCode,
        BigDecimal couponDiscountPercentage,
        QuoteCalculationMethod quoteCalculationMethod,
        Set<QuoteCalculationMethod> enabledQuoteCalculationMethods,
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
                subscriptionPlan, subscriptionBillingCycle, additionalUserSeats, subscriptionActive,
                subscriptionPaidUntil, subscriptionPrice,
                couponCode, couponDiscountPercentage, quoteCalculationMethod, enabledQuoteCalculationMethods,
                defaultSquareMeterPrice, defaultCubicMeterPrice, createdAt, at);
    }

    public Tenant withCompanySettings(boolean assetsRequired, QuoteCalculationMethod calculationMethod,
                                      Set<QuoteCalculationMethod> enabledCalculationMethods,
                                      BigDecimal squareMeterPrice, BigDecimal cubicMeterPrice, Instant at) {
        return new Tenant(id, legalName, tradeName, slug, document, status, logoUrl, primaryColor, assetsRequired,
                subscriptionPlan, subscriptionBillingCycle, additionalUserSeats, subscriptionActive,
                subscriptionPaidUntil, subscriptionPrice,
                couponCode, couponDiscountPercentage, calculationMethod,
                Set.copyOf(enabledCalculationMethods), squareMeterPrice, cubicMeterPrice, createdAt, at);
    }

    public int subscriptionUserLimit() {
        return subscriptionPlan.includedUsers() + additionalUserSeats;
    }
}
