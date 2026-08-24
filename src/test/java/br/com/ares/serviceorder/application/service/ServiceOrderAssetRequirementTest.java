package br.com.ares.serviceorder.application.service;

import br.com.ares.asset.application.port.in.AssetDirectory;
import br.com.ares.customer.application.port.in.CustomerDirectory;
import br.com.ares.identity.application.port.in.TenantUserDirectory;
import br.com.ares.servicecatalog.application.port.in.ServiceCatalogDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.serviceorder.application.port.out.ServiceOrderRepository;
import br.com.ares.serviceorder.domain.model.ServiceOrderPriority;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantSettingsDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderAssetRequirementTest {

    private static final Instant NOW = Instant.parse("2026-08-24T12:00:00Z");

    @Mock ServiceOrderRepository repository;
    @Mock CustomerDirectory customers;
    @Mock AssetDirectory assets;
    @Mock ServiceCatalogDirectory catalog;
    @Mock TenantSettingsDirectory tenantSettings;
    @Mock ServiceOrderStatusDirectory statuses;
    @Mock TenantUserDirectory users;
    @Mock CurrentActorProvider currentActor;
    @Mock AuditLogPort audit;

    private ServiceOrderService service;
    private UUID tenantId;
    private UUID customerId;
    private UUID catalogServiceId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        catalogServiceId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), tenantId, "admin@example.com",
                Set.of("ADMIN"), Set.of("SERVICE_ORDER_CREATE"), null);
        when(currentActor.requiredActor()).thenReturn(actor);
        when(customers.exists(tenantId, customerId)).thenReturn(true);
        when(catalog.allExistAndActive(tenantId, Set.of(catalogServiceId))).thenReturn(true);
        when(tenantSettings.requireAssets(tenantId)).thenReturn(true);
        service = new ServiceOrderService(repository, customers, assets, catalog, tenantSettings, statuses, users, currentActor, audit,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsGeneralServiceOrderWithoutAsset() {
        when(catalog.anyRequiresAsset(tenantId, Set.of(catalogServiceId))).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(command(null));

        assertThat(created.assetId()).isNull();
        verify(assets, never()).belongsToCustomer(any(), any(), any());
    }

    @Test
    void requiresCustomerAssetForMaintenanceService() {
        when(catalog.anyRequiresAsset(tenantId, Set.of(catalogServiceId))).thenReturn(true);

        assertThatThrownBy(() -> service.create(command(null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ativo que receberá a manutenção");
    }

    @Test
    void createsMaintenanceWithoutAssetWhenCompanyDoesNotRequireAssets() {
        when(tenantSettings.requireAssets(tenantId)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(command(null));

        assertThat(created.assetId()).isNull();
        verify(catalog, never()).anyRequiresAsset(any(), any());
        verify(assets, never()).belongsToCustomer(any(), any(), any());
    }

    private ServiceOrderUseCase.CreateOrderCommand command(UUID assetId) {
        var line = new ServiceOrderUseCase.QuoteLineCommand(catalogServiceId, "Serviço cadastrado",
                BigDecimal.ONE, "SERVICO", new BigDecimal("150.00"));
        return new ServiceOrderUseCase.CreateOrderCommand(customerId, assetId, List.of(line), "Atendimento",
                null, ServiceOrderPriority.NORMAL, null, null);
    }
}
