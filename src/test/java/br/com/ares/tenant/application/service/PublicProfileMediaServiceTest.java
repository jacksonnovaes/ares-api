package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.application.port.out.PublicProfileMediaRepository;
import br.com.ares.tenant.domain.model.PublicProfileMediaKind;
import br.com.ares.tenant.domain.model.PublicProfileStoredMedia;
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
    @Mock PublicProfileMediaRepository mediaRepository;
    @Mock AuditLogPort audit;

    private PublicProfileMediaService service;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        UUID tenantId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), tenantId, "admin@example.com",
                Set.of("ADMIN"), Set.of("TENANT_CONFIGURE"), null);
        CurrentActorProvider currentActor = () -> actor;
        service = new PublicProfileMediaService(tenants, mediaRepository, currentActor, audit,
                Clock.fixed(NOW, ZoneOffset.UTC), storage.toString());
        tenant = tenant(tenantId);
        lenient().when(tenants.findById(tenantId)).thenReturn(Optional.of(tenant));
        lenient().when(tenants.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(mediaRepository.findByTenantIdAndKind(any(), any())).thenReturn(Optional.empty());
        lenient().when(mediaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void storesAndLoadsTheImageFromTheDatabase() {
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

        var result = service.store(PublicProfileMediaKind.PROFILE, "minha-foto.png", "image/png", png);

        var saved = ArgumentCaptor.forClass(Tenant.class);
        verify(tenants).save(saved.capture());
        var savedMedia = ArgumentCaptor.forClass(PublicProfileStoredMedia.class);
        verify(mediaRepository).save(savedMedia.capture());
        assertThat(result.path()).startsWith(tenant.id() + "/profile-").endsWith(".png");
        assertThat(saved.getValue().publicProfileImagePath()).isEqualTo(result.path());
        assertThat(savedMedia.getValue().content()).isEqualTo(png);
        assertThat(savedMedia.getValue().contentType()).isEqualTo("image/png");
        assertThat(Files.exists(storage.resolve(result.path()))).isFalse();

        String[] path = result.path().split("/", 2);
        when(mediaRepository.findByTenantIdAndKind(tenant.id(), PublicProfileMediaKind.PROFILE))
                .thenReturn(Optional.of(savedMedia.getValue()));
        var loaded = service.load(path[0], path[1]);
        assertThat(loaded.contentType()).isEqualTo("image/png");
        assertThat(loaded.bytes()).isEqualTo(png);
    }

    @Test
    void migratesALegacyDiskImageToTheDatabaseWhenItIsRead() throws Exception {
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        String filename = "logo-" + UUID.randomUUID() + ".png";
        Path legacyFile = storage.resolve(tenant.id().toString()).resolve(filename);
        Files.createDirectories(legacyFile.getParent());
        Files.write(legacyFile, png);

        var loaded = service.load(tenant.id().toString(), filename);

        var migrated = ArgumentCaptor.forClass(PublicProfileStoredMedia.class);
        verify(mediaRepository).save(migrated.capture());
        assertThat(migrated.getValue().tenantId()).isEqualTo(tenant.id());
        assertThat(migrated.getValue().kind()).isEqualTo(PublicProfileMediaKind.LOGO);
        assertThat(migrated.getValue().content()).isEqualTo(png);
        assertThat(loaded.bytes()).isEqualTo(png);
    }

    @Test
    void storesTheAppearanceLogoInTheDatabaseAndUpdatesTheTenantBrand() {
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

        var result = service.store(PublicProfileMediaKind.BRAND, "marca.png", "image/png", png);

        var savedTenant = ArgumentCaptor.forClass(Tenant.class);
        verify(tenants).save(savedTenant.capture());
        var savedMedia = ArgumentCaptor.forClass(PublicProfileStoredMedia.class);
        verify(mediaRepository).save(savedMedia.capture());
        assertThat(result.path()).startsWith(tenant.id() + "/brand-").endsWith(".png");
        assertThat(savedTenant.getValue().logoUrl()).isEqualTo(result.path());
        assertThat(savedMedia.getValue().kind()).isEqualTo(PublicProfileMediaKind.BRAND);
        assertThat(savedMedia.getValue().content()).isEqualTo(png);
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
                "#2457E6", "#F6F4ED", "#142019", null, null, null, true, 18, NOW, NOW);
    }
}
