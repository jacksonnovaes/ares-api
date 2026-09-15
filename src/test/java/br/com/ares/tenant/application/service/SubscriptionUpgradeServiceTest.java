package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.SubscriptionUpgradeUseCase;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionUpgradeServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-15T12:00:00Z");

    @Mock TenantRepository tenants;
    @Mock AuditLogPort audit;

    private SubscriptionUpgradeUseCase service;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), tenantId, "admin@example.com",
                Set.of("ADMIN"), Set.of("TENANT_CONFIGURE"), null);
        CurrentActorProvider currentActor = () -> actor;
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var pricing = new SubscriptionPricingService("", BigDecimal.ZERO, "", clock);
        service = new SubscriptionUpgradeService(tenants, currentActor, audit, pricing, clock, true);
        when(tenants.findById(tenantId)).thenReturn(Optional.of(tenant(SubscriptionPlan.SOLO)));
        lenient().when(tenants.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void listsOnlyPlansAboveTheCurrentPlan() {
        var options = service.options();

        assertThat(options.plans()).extracting(SubscriptionUpgradeUseCase.PlanOption::code)
                .containsExactly(SubscriptionPlan.PRO, SubscriptionPlan.BUSINESS);
        assertThat(options.upgradeEnabled()).isTrue();
    }

    @Test
    void upgradesWithThePriceCalculatedOnTheServer() {
        var result = service.upgrade(new SubscriptionUpgradeUseCase.UpgradeCommand(
                SubscriptionPlan.PRO, SubscriptionBillingCycle.ANNUAL, 2, true));

        assertThat(result.plan()).isEqualTo(SubscriptionPlan.PRO);
        assertThat(result.userLimit()).isEqualTo(5);
        assertThat(result.price()).isEqualByComparingTo("697.00");
        assertThat(result.paidUntil()).isEqualTo(NOW.plusSeconds(365L * 24 * 60 * 60));
        verify(audit).record(any(), any(), org.mockito.ArgumentMatchers.eq("TENANT_SUBSCRIPTION_UPGRADED"),
                org.mockito.ArgumentMatchers.eq("TENANT"), any(), any());
    }

    @Test
    void rejectsTheCurrentOrALowerPlan() {
        assertThatThrownBy(() -> service.upgrade(new SubscriptionUpgradeUseCase.UpgradeCommand(
                SubscriptionPlan.SOLO, SubscriptionBillingCycle.MONTHLY, 0, true)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("plano superior");
    }

    private Tenant tenant(SubscriptionPlan plan) {
        return new Tenant(tenantId, "Ares Ltda.", "Ares", "ares", "12345678000190",
                TenantStatus.ACTIVE, null, "#2457E6", "#16A085", 12, false, true, plan,
                SubscriptionBillingCycle.MONTHLY, 0, true, NOW.plusSeconds(2_592_000),
                plan.monthlyPrice(), null, BigDecimal.ZERO.setScale(2), QuoteCalculationMethod.QUANTITY,
                EnumSet.allOf(QuoteCalculationMethod.class), null, null, false, null, null, null, null, null,
                null, false, PublicServiceSource.CATALOG, java.util.List.of(), "#2457E6", "#F6F4ED", "#142019",
                null, null, null, true, 18, NOW, NOW);
    }
}
