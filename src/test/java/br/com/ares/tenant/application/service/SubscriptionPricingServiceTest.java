package br.com.ares.tenant.application.service;

import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionPricingServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-27T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void appliesConfiguredCouponWithoutTrustingAClientPrice() {
        var service = new SubscriptionPricingService("bemvindo20", new BigDecimal("20"),
                "2026-12-31", clock);

        var quote = service.quote(SubscriptionPlan.PRO, SubscriptionBillingCycle.MONTHLY, 0,
                " BEMVINDO20 ");

        assertThat(quote.originalPrice()).isEqualByComparingTo("69.90");
        assertThat(quote.discountAmount()).isEqualByComparingTo("13.98");
        assertThat(quote.finalPrice()).isEqualByComparingTo("55.92");
        assertThat(quote.couponCode()).isEqualTo("BEMVINDO20");
    }

    @Test
    void pricesAnnualBillingAndAdditionalUsers() {
        var service = new SubscriptionPricingService("", BigDecimal.ZERO, "", clock);

        var quote = service.quote(SubscriptionPlan.PRO, SubscriptionBillingCycle.ANNUAL, 2, null);

        assertThat(quote.basePrice()).isEqualByComparingTo("699.00");
        assertThat(quote.additionalUsersPrice()).isEqualByComparingTo("258.00");
        assertThat(quote.finalPrice()).isEqualByComparingTo("957.00");
        assertThat(quote.monthlyEquivalent()).isEqualByComparingTo("79.75");
        assertThat(quote.userLimit()).isEqualTo(5);
    }

    @Test
    void rejectsUnknownCoupon() {
        var service = new SubscriptionPricingService("BEMVINDO20", new BigDecimal("20"),
                "2026-12-31", clock);

        assertThatThrownBy(() -> service.quote(SubscriptionPlan.SOLO, SubscriptionBillingCycle.MONTHLY,
                0, "OUTRO"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cupom inválido ou indisponível.");
    }
}
