package br.com.ares.serviceorder.application.service;

import br.com.ares.asset.application.port.in.AssetDirectory;
import br.com.ares.customer.application.port.in.CustomerDirectory;
import br.com.ares.identity.application.port.in.TenantUserDirectory;
import br.com.ares.servicecatalog.application.port.in.ServiceCatalogDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.serviceorder.application.port.out.ServiceOrderRepository;
import br.com.ares.serviceorder.domain.model.ServiceOrder;
import br.com.ares.serviceorder.domain.model.ServiceOrderPriority;
import br.com.ares.serviceorder.domain.model.ServiceOrderStatusDefinition;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderCompletionServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-02T18:00:00Z");

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
    private ServiceOrder order;

    @BeforeEach
    void setUp() {
        UUID tenantId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), tenantId, "admin@example.com",
                Set.of("ADMIN"), Set.of("SERVICE_ORDER_UPDATE"), null);
        when(currentActor.requiredActor()).thenReturn(actor);
        order = new ServiceOrder(UUID.randomUUID(), tenantId, UUID.randomUUID(), null, Set.of(), List.of(),
                "Instalação elétrica", null, "IN_PROGRESS", ServiceOrderPriority.NORMAL,
                new BigDecimal("480.00"), null, null, NOW.minusSeconds(86400), null, null, null,
                NOW.minusSeconds(86400), NOW.minusSeconds(86400));
        when(repository.findByIdAndTenantId(order.id(), tenantId)).thenReturn(Optional.of(order));
        when(statuses.requiredActive(tenantId, "COMPLETED")).thenReturn(new ServiceOrderStatusDefinition(
                UUID.randomUUID(), tenantId, "COMPLETED", "Concluída", true, true, 90, NOW, NOW));
        service = new ServiceOrderService(repository, customers, assets, catalog, tenantSettings, statuses, users,
                currentActor, audit, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void completesWithDeliveryWarrantyAndEstimatedValueAsFinalValue() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var completed = service.changeStatus(order.id(), new ServiceOrderUseCase.ChangeStatusCommand(
                "COMPLETED", null, "Maria da Silva", 90, "Garantia dos serviços executados.",
                "Cliente recebeu e testou o serviço."));

        assertThat(completed.status()).isEqualTo("COMPLETED");
        assertThat(completed.finalValue()).isEqualByComparingTo("480.00");
        assertThat(completed.completedAt()).isEqualTo(NOW);
        assertThat(completed.delivery().receivedBy()).isEqualTo("Maria da Silva");
        assertThat(completed.delivery().warrantyDays()).isEqualTo(90);
        assertThat(completed.delivery().warrantyUntil()).isEqualTo(NOW.plusSeconds(90L * 86400));
        assertThat(completed.delivery().notes()).contains("recebeu");
    }

    @Test
    void requiresTheDeliveryRecipientWhenCompleting() {
        assertThatThrownBy(() -> service.changeStatus(order.id(), new ServiceOrderUseCase.ChangeStatusCommand(
                "COMPLETED", null, " ", 90, null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("quem recebeu");
    }
}
