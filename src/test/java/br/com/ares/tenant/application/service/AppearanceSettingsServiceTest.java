package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.tenant.application.port.in.AppearanceSettingsUseCase;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppearanceSettingsServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");

    @Mock TenantRepository tenants;
    @Mock AuditLogPort audit;

    private AppearanceSettingsService service;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), tenantId, "admin@example.com",
                Set.of("ADMIN"), Set.of("TENANT_CONFIGURE"), null);
        CurrentActorProvider currentActor = () -> actor;
        service = new AppearanceSettingsService(tenants, currentActor, audit,
                Clock.fixed(NOW, ZoneOffset.UTC));
        when(tenants.findById(tenantId)).thenReturn(Optional.of(tenant()));
        when(tenants.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void persistsTheTenantAppearanceInsteadOfUsingBrowserStorage() {
        var result = service.update(new AppearanceSettingsUseCase.UpdateAppearanceCommand(
                "Minha Oficina", "#123ABC", "#ABC123", 18));

        var saved = ArgumentCaptor.forClass(Tenant.class);
        verify(tenants).save(saved.capture());
        assertThat(saved.getValue().tradeName()).isEqualTo("Minha Oficina");
        assertThat(saved.getValue().primaryColor()).isEqualTo("#123ABC");
        assertThat(saved.getValue().secondaryColor()).isEqualTo("#ABC123");
        assertThat(saved.getValue().borderRadius()).isEqualTo(18);
        assertThat(result.tradeName()).isEqualTo("Minha Oficina");
        assertThat(result.borderRadius()).isEqualTo(18);
    }

    private Tenant tenant() {
        return new Tenant(tenantId, "Ares Ltda.", "Ares", "ares", "12345678000190", TenantStatus.ACTIVE,
                null, "#2457E6", "#16A085", 12, false, SubscriptionPlan.SOLO,
                SubscriptionBillingCycle.MONTHLY, 0, true, NOW.plusSeconds(2_592_000),
                new BigDecimal("29.90"), null, BigDecimal.ZERO.setScale(2), QuoteCalculationMethod.QUANTITY,
                EnumSet.allOf(QuoteCalculationMethod.class), null, null, false, null, null, null, null, null,
                null, false, PublicServiceSource.CATALOG, List.of(), "#2457E6", "#F6F4ED", "#142019",
                null, null, null, true, 18, NOW, NOW);
    }
}
