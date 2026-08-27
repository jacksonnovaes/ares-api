package br.com.ares.serviceorder.application.service;

import br.com.ares.asset.application.port.in.AssetUseCase;
import br.com.ares.asset.application.port.in.AssetTypeDirectory;
import br.com.ares.asset.domain.model.Asset;
import br.com.ares.asset.domain.model.AssetType;
import br.com.ares.customer.application.port.in.CustomerUseCase;
import br.com.ares.customer.domain.model.Customer;
import br.com.ares.customer.domain.model.CustomerStatus;
import br.com.ares.customer.domain.model.CustomerType;
import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase;
import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.serviceorder.application.port.out.ServiceOrderEmailSender;
import br.com.ares.serviceorder.domain.model.ServiceOrder;
import br.com.ares.serviceorder.domain.model.ServiceOrderLine;
import br.com.ares.serviceorder.domain.model.ServiceOrderPriority;
import br.com.ares.serviceorder.domain.model.ServiceOrderStatusDefinition;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import br.com.ares.tenant.domain.model.Tenant;
import br.com.ares.tenant.domain.model.TenantStatus;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
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
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderDocumentServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-22T15:30:00Z");

    @Mock ServiceOrderUseCase orders;
    @Mock CustomerUseCase customers;
    @Mock AssetUseCase assets;
    @Mock AssetTypeDirectory assetTypes;
    @Mock ServiceOrderStatusDirectory statuses;
    @Mock TenantManagementUseCase tenants;
    @Mock ServiceOrderEmailSender emailSender;
    @Mock CurrentActorProvider currentActor;
    @Mock AuditLogPort audit;

    private ServiceOrderDocumentService service;
    private Fixture fixture;

    @BeforeEach
    void setUp() {
        service = new ServiceOrderDocumentService(orders, customers, assets, assetTypes, statuses, tenants, emailSender,
                currentActor, audit, Clock.fixed(NOW, ZoneOffset.UTC));
        fixture = fixture();
        when(orders.get(fixture.order.id())).thenReturn(fixture.order);
        when(customers.get(fixture.customer.id())).thenReturn(fixture.customer);
        when(assets.get(fixture.asset.id())).thenReturn(fixture.asset);
        when(assetTypes.required(fixture.tenant.id(), fixture.asset.type())).thenReturn(fixture.assetType);
        when(statuses.required(fixture.tenant.id(), "OPEN")).thenReturn(new ServiceOrderStatusDefinition(
                UUID.randomUUID(), fixture.tenant.id(), "OPEN", "Aberto", true, true, 10, NOW, NOW));
        when(tenants.requiredById(fixture.tenant.id())).thenReturn(fixture.tenant);
    }

    @Test
    void consolidatesPrintableDocumentFromOrderRelationships() {
        var document = service.getDocument(fixture.order.id());

        assertThat(document.order().title()).isEqualTo("Revisão preventiva");
        assertThat(document.company().tradeName()).isEqualTo("Oficina Ares");
        assertThat(document.customer().email()).isEqualTo("cliente@example.com");
        assertThat(document.asset().name()).isEqualTo("Veículo principal");
        assertThat(document.asset().typeName()).isEqualTo("Veículo");
        assertThat(document.quoteLines()).singleElement()
                .extracting(ServiceOrderDocumentUseCase.QuoteLineView::description)
                .isEqualTo("Troca de óleo");
    }

    @Test
    void simulatesEmailUsingCustomerAddressAndAuditsTheOperation() {
        var actor = new AuthenticatedActor(UUID.randomUUID(), fixture.tenant.id(), "admin@example.com",
                Set.of("ADMIN"), Set.of("SERVICE_ORDER_UPDATE"), null);
        when(emailSender.deliveryMode()).thenReturn("SIMULATION");
        when(currentActor.requiredActor()).thenReturn(actor);

        var result = service.sendEmail(fixture.order.id(), new ServiceOrderDocumentUseCase.SendEmailCommand(null));

        assertThat(result.deliveryMode()).isEqualTo("SIMULATION");
        assertThat(result.recipient()).isEqualTo("cliente@example.com");
        assertThat(result.subject()).contains("Ordem de serviço", "Oficina Ares");
        assertThat(result.body()).contains("Revisão preventiva", "Troca de óleo", "R$ 350,00");
        assertThat(result.processedAt()).isEqualTo(NOW);

        var message = ArgumentCaptor.forClass(ServiceOrderEmailSender.EmailMessage.class);
        verify(emailSender).send(message.capture());
        assertThat(message.getValue().recipient()).isEqualTo("cliente@example.com");
        verify(audit).record(fixture.tenant.id(), actor.userId(), "SERVICE_ORDER_EMAIL_PROCESSED",
                "SERVICE_ORDER", fixture.order.id().toString(),
                Map.of("recipient", "cliente@example.com", "deliveryMode", "SIMULATION"));
    }

    @Test
    void requiresRecipientWhenCustomerHasNoEmail() {
        var customerWithoutEmail = new Customer(fixture.customer.id(), fixture.tenant.id(), CustomerType.PERSON,
                "Cliente", "12345678901", null, null, null, CustomerStatus.ACTIVE, NOW, NOW);
        when(customers.get(fixture.customer.id())).thenReturn(customerWithoutEmail);

        assertThatThrownBy(() -> service.sendEmail(fixture.order.id(),
                new ServiceOrderDocumentUseCase.SendEmailCommand("")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Informe um e-mail");
    }

    private Fixture fixture() {
        UUID tenantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        var tenant = new Tenant(tenantId, "Ares Serviços Ltda.", "Oficina Ares", "oficina-ares",
                "12345678000190", TenantStatus.ACTIVE, null, "#2457E6", true,
                SubscriptionPlan.PROFESSIONAL, true, NOW.plusSeconds(2_592_000),
                new BigDecimal("99.90"), null, BigDecimal.ZERO.setScale(2),
                br.com.ares.tenant.domain.model.QuoteCalculationMethod.QUANTITY, null, null, NOW, NOW);
        var customer = new Customer(customerId, tenantId, CustomerType.PERSON, "Maria da Silva",
                "12345678901", "cliente@example.com", "11999999999", null, CustomerStatus.ACTIVE, NOW, NOW);
        var assetType = new AssetType(UUID.randomUUID(), tenantId, "VEHICLE", "Veículo", true, true, NOW, NOW);
        var asset = new Asset(assetId, tenantId, customerId, "VEHICLE", "Veículo principal",
                "Toyota", "Corolla", "ABC123", Map.of(), NOW, NOW);
        var quoteLine = new ServiceOrderLine(serviceId, "Troca de óleo", BigDecimal.ONE,
                "UN", new BigDecimal("350.00"));
        var order = new ServiceOrder(UUID.randomUUID(), tenantId, customerId, assetId, Set.of(serviceId),
                List.of(quoteLine), "Revisão preventiva", "Executar revisão completa", "OPEN",
                ServiceOrderPriority.NORMAL, new BigDecimal("350.00"), null, null, NOW, NOW.plusSeconds(86400),
                null, NOW, NOW);
        return new Fixture(tenant, customer, assetType, asset, order);
    }

    private record Fixture(Tenant tenant, Customer customer, AssetType assetType, Asset asset,
                           ServiceOrder order) {
    }
}
