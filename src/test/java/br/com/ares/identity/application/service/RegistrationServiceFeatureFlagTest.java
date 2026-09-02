package br.com.ares.identity.application.service;

import br.com.ares.identity.application.port.in.RegistrationUseCase;
import br.com.ares.identity.application.port.out.PasswordHasher;
import br.com.ares.identity.application.port.out.UserRepository;
import br.com.ares.identity.domain.service.PasswordPolicy;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.Tenant;
import br.com.ares.tenant.domain.model.TenantStatus;
import br.com.ares.tenant.application.service.SubscriptionPricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceFeatureFlagTest {

    private static final Instant NOW = Instant.parse("2026-08-27T12:00:00Z");

    @Mock TenantManagementUseCase tenants;
    @Mock UserRepository users;
    @Mock PasswordHasher passwordHasher;
    @Mock PasswordPolicy passwordPolicy;
    @Mock AuditLogPort audit;

    private RegistrationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var pricing = new SubscriptionPricingService("", BigDecimal.ZERO, "", clock);
        service = new RegistrationService(tenants, users, passwordHasher, passwordPolicy, audit,
                clock, false, pricing, "2026-08-27", "2026-08-27");
        when(users.existsByEmail(any())).thenReturn(false);
        when(passwordHasher.hash(any())).thenReturn("password-hash");
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tenants.create(any())).thenAnswer(invocation -> {
            TenantManagementUseCase.CreateTenantCommand command = invocation.getArgument(0);
            return new Tenant(UUID.randomUUID(), command.legalName(), command.tradeName(), command.slug(),
                    command.document(), TenantStatus.ACTIVE, command.logoUrl(), command.primaryColor(), true,
                    command.subscriptionPlan(), command.subscriptionBillingCycle(), command.additionalUserSeats(),
                    command.subscriptionActive(), command.subscriptionPaidUntil(), command.subscriptionPrice(),
                    command.couponCode(), command.couponDiscountPercentage(),
                    br.com.ares.tenant.domain.model.QuoteCalculationMethod.QUANTITY,
                    java.util.EnumSet.allOf(br.com.ares.tenant.domain.model.QuoteCalculationMethod.class),
                    null, null, false, null, null, null, null, null, null, false,
                    br.com.ares.tenant.domain.model.PublicServiceSource.CATALOG, java.util.List.of(),
                    "#2457E6", "#F6F4ED", "#142019", null, null, null, true, 18, NOW, NOW);
        });
    }

    @Test
    void ignoresPaymentApprovalSentByTheBrowserWhenFeatureIsDisabled() {
        var result = service.register(new RegistrationUseCase.RegisterTenantAdminCommand(
                "Ares Teste Ltda.", "Ares Teste", "ares-feature-off", "12345678000190",
                null, "#2457E6", SubscriptionPlan.PRO, SubscriptionBillingCycle.MONTHLY, 0,
                "11999999999", null, true,
                true, true, "2026-08-27", "2026-08-27", "127.0.0.1", "JUnit",
                "Administrador", "feature-off@example.com", "SenhaForte#123", "SenhaForte#123"));

        var command = ArgumentCaptor.forClass(TenantManagementUseCase.CreateTenantCommand.class);
        verify(tenants).create(command.capture());
        assertThat(command.getValue().subscriptionActive()).isFalse();
        assertThat(command.getValue().subscriptionPaidUntil()).isNull();
        assertThat(result.subscriptionActive()).isFalse();
        assertThat(result.subscriptionPaidUntil()).isNull();
    }
}
