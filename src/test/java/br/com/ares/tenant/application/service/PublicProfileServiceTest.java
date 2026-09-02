package br.com.ares.tenant.application.service;

import br.com.ares.servicecatalog.application.port.in.ServiceCatalogDirectory;
import br.com.ares.servicecatalog.domain.model.CatalogService;
import br.com.ares.servicecatalog.domain.model.CatalogServiceType;
import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.PublicProfileUseCase;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.Tenant;
import br.com.ares.tenant.domain.model.TenantStatus;
import br.com.ares.tenant.domain.model.PublicServiceSource;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicProfileServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-01T12:00:00Z");

    @Mock TenantRepository tenants;
    @Mock ServiceCatalogDirectory catalog;
    @Mock AuditLogPort audit;

    private PublicProfileService service;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        UUID tenantId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), tenantId, "admin@example.com",
                Set.of("ADMIN"), Set.of("TENANT_CONFIGURE"), null);
        CurrentActorProvider currentActor = () -> actor;
        service = new PublicProfileService(tenants, catalog, currentActor, audit,
                Clock.fixed(NOW, ZoneOffset.UTC));
        tenant = tenant(tenantId, false, false);
        lenient().when(tenants.findById(tenantId)).thenReturn(Optional.of(tenant));
        lenient().when(tenants.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void publishesACompleteProfileAndNormalizesContactData() {
        var result = service.update(new PublicProfileUseCase.UpdateProfileCommand(true,
                "  Serviços elétricos com segurança  ", " Atendimento residencial e comercial. ",
                "(11) 99999-9999", "CONTATO@EXAMPLE.COM", " Campinas - SP ",
                "Campinas e região", true, PublicServiceSource.CATALOG, List.of(),
                "#336699", "#F6F4ED", "#142019", true, 25));

        var saved = ArgumentCaptor.forClass(Tenant.class);
        verify(tenants).save(saved.capture());
        assertThat(saved.getValue().publicPageEnabled()).isTrue();
        assertThat(saved.getValue().publicWhatsapp()).isEqualTo("11999999999");
        assertThat(result.email()).isEqualTo("contato@example.com");
        assertThat(result.showPrices()).isTrue();
    }

    @Test
    void refusesToPublishWithoutAWhatsappContact() {
        assertThatThrownBy(() -> service.update(new PublicProfileUseCase.UpdateProfileCommand(true,
                "Título", "Apresentação", null, null, null, null, false,
                PublicServiceSource.CATALOG, List.of(), "#2457E6", "#F6F4ED", "#142019", true, 18)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("WhatsApp");
    }

    @Test
    void returnsOnlyTheDataPreparedForThePublishedPage() {
        Tenant published = tenant(tenant.id(), true, false);
        when(tenants.findBySlug("joao-eletricista")).thenReturn(Optional.of(published));
        when(catalog.listActive(published.id())).thenReturn(List.of(new CatalogService(UUID.randomUUID(),
                published.id(), "Instalação elétrica", "Instalação residencial", new BigDecimal("180.00"),
                120, CatalogServiceType.GENERAL, true, NOW, NOW)));

        var result = service.findPublished("JOAO-ELETRICISTA");

        assertThat(result.tradeName()).isEqualTo("João Eletricista");
        assertThat(result.services()).hasSize(1);
        assertThat(result.services().getFirst().basePrice()).isNull();
    }

    private Tenant tenant(UUID id, boolean published, boolean showPrices) {
        return new Tenant(id, "João Serviços Elétricos", "João Eletricista", "joao-eletricista",
                "12345678901", TenantStatus.ACTIVE, null, "#2457E6", false, SubscriptionPlan.SOLO,
                SubscriptionBillingCycle.MONTHLY, 0, true, NOW.plusSeconds(2_592_000),
                new BigDecimal("29.90"), null, BigDecimal.ZERO.setScale(2), QuoteCalculationMethod.QUANTITY,
                EnumSet.allOf(QuoteCalculationMethod.class), null, null, published,
                "Serviços elétricos com segurança", "Atendimento residencial e comercial.", "11999999999",
                "contato@example.com", "Campinas - SP", "Campinas e região", showPrices,
                PublicServiceSource.CATALOG, List.of(), "#2457E6", "#F6F4ED", "#142019", null, null, null,
                true, 18, NOW, NOW);
    }
}
