package br.com.ares.tenant.application.service;

import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

@Service
public class SubscriptionPricingService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");
    public static final BigDecimal ADDITIONAL_USER_MONTHLY_PRICE = new BigDecimal("12.90");
    public static final BigDecimal ADDITIONAL_USER_ANNUAL_PRICE = new BigDecimal("129.00");

    private final String configuredCouponCode;
    private final BigDecimal configuredDiscountPercentage;
    private final LocalDate configuredValidUntil;
    private final Clock clock;

    public SubscriptionPricingService(
            @Value("${ares.promotions.coupon-code:}") String couponCode,
            @Value("${ares.promotions.discount-percentage:0}") BigDecimal discountPercentage,
            @Value("${ares.promotions.valid-until:}") String validUntil,
            Clock clock
    ) {
        this.configuredCouponCode = normalizeCoupon(couponCode);
        this.configuredDiscountPercentage = requireValidPercentage(discountPercentage);
        this.configuredValidUntil = validUntil == null || validUntil.isBlank()
                ? null : LocalDate.parse(validUntil.trim());
        this.clock = clock;
    }

    public SubscriptionPriceQuote quote(SubscriptionPlan plan, SubscriptionBillingCycle billingCycle,
                                        int additionalUserSeats, String couponCode) {
        if (additionalUserSeats < 0 || additionalUserSeats > 100) {
            throw BusinessException.badRequest("invalid_additional_user_seats",
                    "A quantidade de usuários adicionais deve estar entre 0 e 100.");
        }
        BigDecimal basePrice = plan.priceFor(billingCycle).setScale(2, RoundingMode.HALF_UP);
        BigDecimal additionalUsersPrice = additionalUserPriceFor(billingCycle)
                .multiply(BigDecimal.valueOf(additionalUserSeats)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal originalPrice = basePrice.add(additionalUsersPrice).setScale(2, RoundingMode.HALF_UP);
        String normalizedCoupon = normalizeCoupon(couponCode);
        if (normalizedCoupon.isBlank()) {
            return quote(plan, billingCycle, additionalUserSeats, basePrice, additionalUsersPrice,
                    originalPrice, BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2),
                    originalPrice, null, false);
        }
        if (!couponEnabled() || !configuredCouponCode.equals(normalizedCoupon)) {
            throw BusinessException.badRequest("invalid_coupon", "Cupom inválido ou indisponível.");
        }

        BigDecimal discountAmount = originalPrice
                .multiply(configuredDiscountPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal finalPrice = originalPrice.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2);
        return quote(plan, billingCycle, additionalUserSeats, basePrice, additionalUsersPrice,
                originalPrice, configuredDiscountPercentage, discountAmount, finalPrice,
                configuredCouponCode, true);
    }

    public BigDecimal additionalUserPriceFor(SubscriptionBillingCycle billingCycle) {
        return billingCycle == SubscriptionBillingCycle.ANNUAL
                ? ADDITIONAL_USER_ANNUAL_PRICE : ADDITIONAL_USER_MONTHLY_PRICE;
    }

    public boolean couponEnabled() {
        return !configuredCouponCode.isBlank()
                && configuredDiscountPercentage.signum() > 0
                && (configuredValidUntil == null
                || !LocalDate.now(clock.withZone(BUSINESS_ZONE)).isAfter(configuredValidUntil));
    }

    private BigDecimal requireValidPercentage(BigDecimal value) {
        BigDecimal percentage = value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
        if (percentage.signum() < 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalStateException("PROMOTION_DISCOUNT_PERCENTAGE must be between 0 and 100");
        }
        return percentage;
    }

    private static String normalizeCoupon(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private SubscriptionPriceQuote quote(SubscriptionPlan plan, SubscriptionBillingCycle billingCycle,
                                         int additionalUserSeats, BigDecimal basePrice,
                                         BigDecimal additionalUsersPrice, BigDecimal originalPrice,
                                         BigDecimal discountPercentage, BigDecimal discountAmount,
                                         BigDecimal finalPrice, String couponCode, boolean couponApplied) {
        BigDecimal monthlyEquivalent = billingCycle == SubscriptionBillingCycle.ANNUAL
                ? finalPrice.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
                : finalPrice;
        return new SubscriptionPriceQuote(plan, billingCycle, additionalUserSeats,
                plan.includedUsers() + additionalUserSeats, basePrice, additionalUsersPrice,
                originalPrice, discountPercentage, discountAmount, finalPrice, monthlyEquivalent,
                couponCode, couponApplied);
    }

    public record SubscriptionPriceQuote(
            SubscriptionPlan plan,
            SubscriptionBillingCycle billingCycle,
            int additionalUserSeats,
            int userLimit,
            BigDecimal basePrice,
            BigDecimal additionalUsersPrice,
            BigDecimal originalPrice,
            BigDecimal discountPercentage,
            BigDecimal discountAmount,
            BigDecimal finalPrice,
            BigDecimal monthlyEquivalent,
            String couponCode,
            boolean couponApplied
    ) {
    }
}
