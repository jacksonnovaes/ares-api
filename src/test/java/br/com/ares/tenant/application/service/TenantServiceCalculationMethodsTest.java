package br.com.ares.tenant.application.service;

import br.com.ares.asset.application.port.in.AssetTypeDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusDirectory;
import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.CompanySettingsUseCase;
import br.com.ares.tenant.application.port.out.TenantRepository;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantServiceCalculationMethodsTest {

    private static final Instant NOW = Instant.parse("2026-08-30T12:00:00Z");

    @Mock TenantRepository repository;
    @Mock AssetTypeDirectory assetTypes;
    @Mock ServiceOrderStatusDirectory orderStatuses;
    @Mock AuditLogPort audit;

    private TenantService service;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        UUID tenantId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), tenantId, "admin@example.com",
                Set.of("ADMIN"), Set.of("TENANT_CONFIGURE"), null);
        CurrentActorProvider currentActor = () -> actor;
        service = new TenantService(repository, assetTypes, orderStatuses, currentActor, audit,
                Clock.fixed(NOW, ZoneOffset.UTC));
        tenant = new Tenant(tenantId, "Ares Ltda.", "Ares", "ares", "12345678000190",
                TenantStatus.ACTIVE, null, "#2457E6", true, SubscriptionPlan.SOLO,
                SubscriptionBillingCycle.MONTHLY, 0, true, NOW.plusSeconds(2_592_000),
                new BigDecimal("29.90"), null, BigDecimal.ZERO.setScale(2),
                QuoteCalculationMethod.QUANTITY, EnumSet.allOf(QuoteCalculationMethod.class),
                new BigDecimal("80.00"), new BigDecimal("250.00"), NOW, NOW);
        when(repository.findById(tenantId)).thenReturn(Optional.of(tenant));
        lenient().when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void savesOnlyTheCalculationMethodsSelectedForTheQuoteMenu() {
        var result = service.update(new CompanySettingsUseCase.UpdateCompanySettingsCommand(
                false, QuoteCalculationMethod.CUBIC_METER,
                Set.of(QuoteCalculationMethod.QUANTITY, QuoteCalculationMethod.CUBIC_METER),
                null, new BigDecimal("275.00")));

        assertThat(result.quoteCalculationMethod()).isEqualTo(QuoteCalculationMethod.CUBIC_METER);
        assertThat(result.enabledQuoteCalculationMethods())
                .containsExactlyInAnyOrder(QuoteCalculationMethod.QUANTITY, QuoteCalculationMethod.CUBIC_METER);
        assertThat(result.defaultCubicMeterPrice()).isEqualByComparingTo("275.00");
    }

    @Test
    void rejectsADefaultCalculationMethodThatIsNotEnabled() {
        assertThatThrownBy(() -> service.update(new CompanySettingsUseCase.UpdateCompanySettingsCommand(
                true, QuoteCalculationMethod.SQUARE_METER, Set.of(QuoteCalculationMethod.QUANTITY),
                new BigDecimal("80.00"), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("métodos selecionados");
    }

    @Test
    void requiresAPriceForEveryEnabledMeasuredMethod() {
        assertThatThrownBy(() -> service.update(new CompanySettingsUseCase.UpdateCompanySettingsCommand(
                true, QuoteCalculationMethod.QUANTITY,
                Set.of(QuoteCalculationMethod.QUANTITY, QuoteCalculationMethod.SQUARE_METER),
                null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("metro quadrado");
    }
}
