package br.com.ares.tenant.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
        String secondaryColor,
        int borderRadius,
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
        boolean publicPageEnabled,
        String publicHeadline,
        String publicDescription,
        String publicWhatsapp,
        String publicEmail,
        String publicCity,
        String publicServiceArea,
        boolean publicShowPrices,
        PublicServiceSource publicServiceSource,
        List<PublicProfileManualService> publicManualServices,
        String publicAccentColor,
        String publicBackgroundColor,
        String publicTextColor,
        String publicProfileImagePath,
        String publicLogoPath,
        String publicBackgroundImagePath,
        boolean publicShowLogo,
        int publicBackgroundOverlayPercentage,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean isActive() {
        return status == TenantStatus.ACTIVE && subscriptionActive;
    }

    public Tenant withBrandLogo(String value, Instant at) {
        return new Tenant(id, legalName, tradeName, slug, document, status, value, primaryColor, secondaryColor,
                borderRadius, requireAssets,
                subscriptionPlan, subscriptionBillingCycle, additionalUserSeats, subscriptionActive,
                subscriptionPaidUntil, subscriptionPrice, couponCode, couponDiscountPercentage,
                quoteCalculationMethod, enabledQuoteCalculationMethods, defaultSquareMeterPrice,
                defaultCubicMeterPrice, publicPageEnabled, publicHeadline, publicDescription, publicWhatsapp,
                publicEmail, publicCity, publicServiceArea, publicShowPrices, publicServiceSource,
                publicManualServices, publicAccentColor, publicBackgroundColor, publicTextColor,
                publicProfileImagePath, publicLogoPath, publicBackgroundImagePath, publicShowLogo,
                publicBackgroundOverlayPercentage, createdAt, at);
    }

    public Tenant withAppearance(String name, String primary, String secondary, int radius, Instant at) {
        return new Tenant(id, legalName, name, slug, document, status, logoUrl, primary, secondary, radius,
                requireAssets, subscriptionPlan, subscriptionBillingCycle, additionalUserSeats, subscriptionActive,
                subscriptionPaidUntil, subscriptionPrice, couponCode, couponDiscountPercentage,
                quoteCalculationMethod, enabledQuoteCalculationMethods, defaultSquareMeterPrice,
                defaultCubicMeterPrice, publicPageEnabled, publicHeadline, publicDescription, publicWhatsapp,
                publicEmail, publicCity, publicServiceArea, publicShowPrices, publicServiceSource,
                publicManualServices, publicAccentColor, publicBackgroundColor, publicTextColor,
                publicProfileImagePath, publicLogoPath, publicBackgroundImagePath, publicShowLogo,
                publicBackgroundOverlayPercentage, createdAt, at);
    }

    public Tenant withRequireAssets(boolean value, Instant at) {
        return new Tenant(id, legalName, tradeName, slug, document, status, logoUrl, primaryColor, secondaryColor,
                borderRadius, value,
                subscriptionPlan, subscriptionBillingCycle, additionalUserSeats, subscriptionActive,
                subscriptionPaidUntil, subscriptionPrice,
                couponCode, couponDiscountPercentage, quoteCalculationMethod, enabledQuoteCalculationMethods,
                defaultSquareMeterPrice, defaultCubicMeterPrice, publicPageEnabled, publicHeadline,
                publicDescription, publicWhatsapp, publicEmail, publicCity, publicServiceArea, publicShowPrices,
                publicServiceSource, publicManualServices, publicAccentColor, publicBackgroundColor, publicTextColor,
                publicProfileImagePath, publicLogoPath, publicBackgroundImagePath, publicShowLogo,
                publicBackgroundOverlayPercentage,
                createdAt, at);
    }

    public Tenant withCompanySettings(boolean assetsRequired, QuoteCalculationMethod calculationMethod,
                                      Set<QuoteCalculationMethod> enabledCalculationMethods,
                                      BigDecimal squareMeterPrice, BigDecimal cubicMeterPrice, Instant at) {
        return new Tenant(id, legalName, tradeName, slug, document, status, logoUrl, primaryColor, secondaryColor,
                borderRadius, assetsRequired,
                subscriptionPlan, subscriptionBillingCycle, additionalUserSeats, subscriptionActive,
                subscriptionPaidUntil, subscriptionPrice,
                couponCode, couponDiscountPercentage, calculationMethod,
                Set.copyOf(enabledCalculationMethods), squareMeterPrice, cubicMeterPrice, publicPageEnabled,
                publicHeadline, publicDescription, publicWhatsapp, publicEmail, publicCity, publicServiceArea,
                publicShowPrices, publicServiceSource, publicManualServices, publicAccentColor,
                publicBackgroundColor, publicTextColor, publicProfileImagePath, publicLogoPath,
                publicBackgroundImagePath, publicShowLogo, publicBackgroundOverlayPercentage, createdAt, at);
    }

    public Tenant withPublicProfile(boolean enabled, String headline, String description, String whatsapp,
                                    String email, String city, String serviceArea, boolean showPrices,
                                    PublicServiceSource serviceSource,
                                    List<PublicProfileManualService> manualServices,
                                    String accentColor, String backgroundColor, String textColor,
                                    boolean showLogo, int backgroundOverlayPercentage, Instant at) {
        return new Tenant(id, legalName, tradeName, slug, document, status, logoUrl, primaryColor, secondaryColor,
                borderRadius, requireAssets,
                subscriptionPlan, subscriptionBillingCycle, additionalUserSeats, subscriptionActive,
                subscriptionPaidUntil, subscriptionPrice, couponCode, couponDiscountPercentage,
                quoteCalculationMethod, enabledQuoteCalculationMethods, defaultSquareMeterPrice,
                defaultCubicMeterPrice, enabled, headline, description, whatsapp, email, city, serviceArea,
                showPrices, serviceSource, List.copyOf(manualServices), accentColor, backgroundColor, textColor,
                publicProfileImagePath, publicLogoPath, publicBackgroundImagePath, showLogo,
                backgroundOverlayPercentage, createdAt, at);
    }

    public Tenant withPublicMedia(String profileImagePath, String logoPath, String backgroundImagePath, Instant at) {
        return new Tenant(id, legalName, tradeName, slug, document, status, logoUrl, primaryColor, secondaryColor,
                borderRadius, requireAssets,
                subscriptionPlan, subscriptionBillingCycle, additionalUserSeats, subscriptionActive,
                subscriptionPaidUntil, subscriptionPrice, couponCode, couponDiscountPercentage,
                quoteCalculationMethod, enabledQuoteCalculationMethods, defaultSquareMeterPrice,
                defaultCubicMeterPrice, publicPageEnabled, publicHeadline, publicDescription, publicWhatsapp,
                publicEmail, publicCity, publicServiceArea, publicShowPrices, publicServiceSource,
                publicManualServices, publicAccentColor, publicBackgroundColor, publicTextColor, profileImagePath,
                logoPath, backgroundImagePath, publicShowLogo, publicBackgroundOverlayPercentage, createdAt, at);
    }

    public int subscriptionUserLimit() {
        return subscriptionPlan.includedUsers() + additionalUserSeats;
    }
}
