package br.com.ares.identity.application.service;

import br.com.ares.asset.application.port.out.AssetTypeRepository;
import br.com.ares.asset.domain.model.AssetType;
import br.com.ares.identity.application.port.in.RegistrationUseCase;
import br.com.ares.serviceorder.application.port.out.ServiceOrderStatusRepository;
import br.com.ares.serviceorder.domain.model.ServiceOrderStatusDefinition;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RegistrationAssetTypesIntegrationTest {

    @Autowired RegistrationUseCase registration;
    @Autowired AssetTypeRepository assetTypes;
    @Autowired ServiceOrderStatusRepository orderStatuses;

    @Test
    void provisionsDefaultAssetTypesWhenATenantIsRegistered() {
        var result = registration.register(new RegistrationUseCase.RegisterTenantAdminCommand(
                "Ares Testes Ltda.", "Ares Testes", "ares-testes", "12345678000190",
                null, "#2457E6", SubscriptionPlan.PRO, SubscriptionBillingCycle.MONTHLY, 0,
                "11999999999", "BEMVINDO20", true,
                true, true, "2026-08-27", "2026-08-27", "127.0.0.1", "JUnit",
                "Admin Teste", "admin-integration@example.com",
                "SenhaForte#123", "SenhaForte#123"));

        assertThat(result.plan()).isEqualTo(SubscriptionPlan.PRO);
        assertThat(result.subscriptionActive()).isTrue();
        assertThat(result.subscriptionPaidUntil()).isNotNull();
        assertThat(result.price()).isEqualByComparingTo("55.92");
        assertThat(result.couponCode()).isEqualTo("BEMVINDO20");

        assertThat(assetTypes.findAllByTenantId(result.tenantId()))
                .hasSize(6)
                .allMatch(AssetType::systemDefault)
                .extracting(AssetType::code)
                .containsExactlyInAnyOrder("VEHICLE", "PHONE", "COMPUTER", "EQUIPMENT", "PROPERTY", "OTHER");
        assertThat(orderStatuses.findAllByTenantId(result.tenantId()))
                .hasSize(5)
                .allMatch(ServiceOrderStatusDefinition::systemDefault)
                .allMatch(ServiceOrderStatusDefinition::active)
                .extracting(ServiceOrderStatusDefinition::code)
                .containsExactly("OPEN", "ANALYSIS", "EXECUTION", "BLOCKED", "COMPLETED");
    }
}
