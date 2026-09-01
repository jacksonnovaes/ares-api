package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.PublicProfileMediaKind;
import br.com.ares.tenant.domain.model.PublicServiceSource;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.Tenant;
import br.com.ares.tenant.domain.model.TenantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PublicProfileMediaServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-01T12:00:00Z");

    @TempDir Path storage;
    @Mock TenantRepository tenants;
    @Mock AuditLogPort audit;

    private PublicProfileMediaService service;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        UUID tenantId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), tenantId, "admin@example.com",
                Set.of("ADMIN"), Set.of("TENANT_CONFIGURE"), null);
        CurrentActorProvider currentActor = () -> actor;
        service = new PublicProfileMediaService(tenants, currentActor, audit,
                Clock.fixed(NOW, ZoneOffset.UTC), storage.toString());
        tenant = tenant(tenantId);
        lenient().when(tenants.findById(tenantId)).thenReturn(Optional.of(tenant));
        lenient().when(tenants.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void storesTheImageOnDiskAndItsRelativePathOnTheTenant() throws Exception {
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

        var result = service.store(PublicProfileMediaKind.LOGO, "minha-logo.png", "image/png", png);

        var saved = ArgumentCaptor.forClass(Tenant.class);
        verify(tenants).save(saved.capture());
        assertThat(result.path()).startsWith(tenant.id() + "/logo-").endsWith(".png");
        assertThat(saved.getValue().publicLogoPath()).isEqualTo(result.path());
        assertThat(Files.readAllBytes(storage.resolve(result.path()))).isEqualTo(png);

        String[] path = result.path().split("/", 2);
        var loaded = service.load(path[0], path[1]);
        assertThat(loaded.contentType()).isEqualTo("image/png");
        assertThat(loaded.bytes()).isEqualTo(png);
    }

    @Test
    void rejectsContentThatOnlyPretendsToBeAnImage() {
        assertThatThrownBy(() -> service.store(PublicProfileMediaKind.BACKGROUND, "fundo.png", "image/png",
                "not-an-image".getBytes()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PNG, JPG ou WebP");
    }

    private Tenant tenant(UUID id) {
        return new Tenant(id, "Ares Ltda.", "Ares", "ares", "12345678000190", TenantStatus.ACTIVE,
                null, "#2457E6", false, SubscriptionPlan.SOLO, SubscriptionBillingCycle.MONTHLY, 0, true,
                NOW.plusSeconds(2_592_000), new BigDecimal("29.90"), null, BigDecimal.ZERO.setScale(2),
                QuoteCalculationMethod.QUANTITY, EnumSet.allOf(QuoteCalculationMethod.class), null, null,
                false, null, null, null, null, null, null, false, PublicServiceSource.CATALOG, List.of(),
                "#2457E6", "#F6F4ED", "#142019", null, null, true, 18, NOW, NOW);
    }
}
