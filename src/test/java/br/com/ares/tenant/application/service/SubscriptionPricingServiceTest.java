package br.com.ares.tenant.application.service;

import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
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

        var quote = service.quote(SubscriptionPlan.PROFESSIONAL, " BEMVINDO20 ");

        assertThat(quote.originalPrice()).isEqualByComparingTo("99.90");
        assertThat(quote.discountAmount()).isEqualByComparingTo("19.98");
        assertThat(quote.finalPrice()).isEqualByComparingTo("79.92");
        assertThat(quote.couponCode()).isEqualTo("BEMVINDO20");
    }

    @Test
    void rejectsUnknownCoupon() {
        var service = new SubscriptionPricingService("BEMVINDO20", new BigDecimal("20"),
                "2026-12-31", clock);

        assertThatThrownBy(() -> service.quote(SubscriptionPlan.ESSENTIAL, "OUTRO"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cupom inválido ou indisponível.");
    }
}
