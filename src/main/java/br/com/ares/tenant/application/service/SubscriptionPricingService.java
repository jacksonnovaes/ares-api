package br.com.ares.tenant.application.service;

import br.com.ares.shared.domain.BusinessException;
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

    public SubscriptionPriceQuote quote(SubscriptionPlan plan, String couponCode) {
        BigDecimal originalPrice = plan.monthlyPrice().setScale(2, RoundingMode.HALF_UP);
        String normalizedCoupon = normalizeCoupon(couponCode);
        if (normalizedCoupon.isBlank()) {
            return new SubscriptionPriceQuote(plan, originalPrice, BigDecimal.ZERO.setScale(2),
                    BigDecimal.ZERO.setScale(2), originalPrice, null, false);
        }
        if (!couponEnabled() || !configuredCouponCode.equals(normalizedCoupon)) {
            throw BusinessException.badRequest("invalid_coupon", "Cupom inválido ou indisponível.");
        }

        BigDecimal discountAmount = originalPrice
                .multiply(configuredDiscountPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal finalPrice = originalPrice.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2);
        return new SubscriptionPriceQuote(plan, originalPrice, configuredDiscountPercentage,
                discountAmount, finalPrice, configuredCouponCode, true);
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

    public record SubscriptionPriceQuote(
            SubscriptionPlan plan,
            BigDecimal originalPrice,
            BigDecimal discountPercentage,
            BigDecimal discountAmount,
            BigDecimal finalPrice,
            String couponCode,
            boolean couponApplied
    ) {
    }
}
