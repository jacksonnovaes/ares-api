package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.PublicServiceSource;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.Tenant;
import br.com.ares.tenant.domain.model.TenantStatus;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SuperAdminTenantServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-08T12:00:00Z");

    @Mock TenantRepository tenants;
    @Mock AuditLogPort audit;

    private UUID actorTenantId;
    private SuperAdminTenantService service;

    @BeforeEach
    void setUp() {
        actorTenantId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), actorTenantId, "owner@aresapp.tech",
                Set.of("SUPER_ADMIN"), Set.of(), null);
        CurrentActorProvider currentActor = () -> actor;
        service = new SuperAdminTenantService(tenants, currentActor, audit, Clock.fixed(NOW, ZoneOffset.UTC));
        lenient().when(tenants.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void enablesTenantAndItsSubscriptionForManualApproval() {
        Tenant tenant = tenant(UUID.randomUUID(), TenantStatus.BLOCKED, false);
        when(tenants.findById(tenant.id())).thenReturn(Optional.of(tenant));

        var result = service.setAccess(tenant.id(), true);

        assertThat(result.accessEnabled()).isTrue();
        assertThat(result.status()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(result.subscriptionActive()).isTrue();
        verify(audit).record(eq(tenant.id()), any(), eq("TENANT_ACCESS_ENABLED"), eq("TENANT"),
                eq(tenant.id().toString()), any());
    }

    @Test
    void disablesTenantAndItsSubscription() {
        Tenant tenant = tenant(UUID.randomUUID(), TenantStatus.ACTIVE, true);
        when(tenants.findById(tenant.id())).thenReturn(Optional.of(tenant));

        var result = service.setAccess(tenant.id(), false);

        assertThat(result.accessEnabled()).isFalse();
        assertThat(result.status()).isEqualTo(TenantStatus.BLOCKED);
        assertThat(result.subscriptionActive()).isFalse();
    }

    @Test
    void preventsSuperAdminFromBlockingOwnTenant() {
        assertThatThrownBy(() -> service.setAccess(actorTenantId, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("própria conta");
    }

    private Tenant tenant(UUID id, TenantStatus status, boolean subscriptionActive) {
        return new Tenant(id, "Empresa Teste Ltda.", "Empresa Teste", "empresa-teste", "12345678000190",
                status, null, "#2457E6", "#16A085", 12, false, true, SubscriptionPlan.SOLO,
                SubscriptionBillingCycle.MONTHLY, 0, subscriptionActive, null, new BigDecimal("29.90"),
                null, BigDecimal.ZERO.setScale(2), QuoteCalculationMethod.QUANTITY,
                EnumSet.allOf(QuoteCalculationMethod.class), null, null, false, null, null, null, null,
                null, null, false, PublicServiceSource.CATALOG, java.util.List.of(), "#2457E6", "#F6F4ED",
                "#142019", null, null, null, true, 18, NOW.minusSeconds(3600), NOW.minusSeconds(3600));
    }
}
