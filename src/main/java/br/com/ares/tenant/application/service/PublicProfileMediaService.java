package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.PublicProfileMediaUseCase;
import br.com.ares.tenant.application.port.out.PublicProfileMediaRepository;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.PublicProfileMediaKind;
import br.com.ares.tenant.domain.model.PublicProfileStoredMedia;
import br.com.ares.tenant.domain.model.Tenant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

@Service
public class PublicProfileMediaService implements PublicProfileMediaUseCase {

    static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final TenantRepository tenants;
    private final PublicProfileMediaRepository mediaRepository;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;
    private final Path root;

    public PublicProfileMediaService(TenantRepository tenants, PublicProfileMediaRepository mediaRepository,
                                     CurrentActorProvider currentActor, AuditLogPort audit,
                                     Clock clock,
                                     @Value("${ares.storage.public-profile-media-root:./data/public-profile-media}")
                                     String storageRoot) {
        this.tenants = tenants;
        this.mediaRepository = mediaRepository;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
        this.root = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public StoredMedia store(PublicProfileMediaKind kind, String originalFilename, String declaredContentType,
                             byte[] content) {
        if (content == null || content.length == 0 || content.length > MAX_FILE_SIZE) {
            throw BusinessException.badRequest("public_profile_media_size_invalid",
                    "Envie uma imagem de até 5 MB.");
        }
        DetectedImage image = detect(content);
        var actor = currentActor.requiredActor();
        Tenant tenant = required(actor.tenantId());
        String filename = kind.name().toLowerCase() + "-" + UUID.randomUUID() + "." + image.extension();
        String relativePath = tenant.id() + "/" + filename;
        String previousPath = pathOf(tenant, kind);
        Instant now = clock.instant();
        var existing = mediaRepository.findByTenantIdAndKind(tenant.id(), kind);
        mediaRepository.save(new PublicProfileStoredMedia(existing.map(PublicProfileStoredMedia::id)
                .orElseGet(UUID::randomUUID), tenant.id(), kind, filename, image.contentType(), content,
                content.length, existing.map(PublicProfileStoredMedia::createdAt).orElse(now), now));
        tenants.save(withPath(tenant, kind, relativePath, now));
        deleteQuietly(previousPath);
        audit.record(actor.tenantId(), actor.userId(), updatedEvent(kind), "TENANT",
                actor.tenantId().toString(), Map.of("kind", kind.name(), "contentType", image.contentType()));
        return new StoredMedia(kind, relativePath);
    }

    @Override
    @Transactional
    public void remove(PublicProfileMediaKind kind) {
        var actor = currentActor.requiredActor();
        Tenant tenant = required(actor.tenantId());
        String previousPath = pathOf(tenant, kind);
        mediaRepository.deleteByTenantIdAndKind(tenant.id(), kind);
        tenants.save(withPath(tenant, kind, null, clock.instant()));
        deleteQuietly(previousPath);
        audit.record(actor.tenantId(), actor.userId(), removedEvent(kind), "TENANT",
                actor.tenantId().toString(), Map.of("kind", kind.name()));
    }

    @Override
    @Transactional
    public MediaContent load(String tenantDirectory, String filename) {
        if (!tenantDirectory.matches("[0-9a-fA-F-]{36}")
                || !filename.matches("(?:brand|profile|logo|background)-[0-9a-fA-F-]{36}\\.(?:png|jpg|webp)")) {
            throw BusinessException.notFound("public_profile_media_not_found", "Imagem não encontrada.");
        }
        UUID tenantId;
        try {
            tenantId = UUID.fromString(tenantDirectory);
        } catch (IllegalArgumentException exception) {
            throw BusinessException.notFound("public_profile_media_not_found", "Imagem não encontrada.");
        }
        PublicProfileMediaKind kind = kindOf(filename);
        var stored = mediaRepository.findByTenantIdAndKind(tenantId, kind);
        if (stored.isPresent() && stored.get().filename().equals(filename)) {
            return new MediaContent(stored.get().content(), stored.get().contentType());
        }

        // Compatibilidade com imagens salvas em volume antes da migração V21.
        Path path = resolve(tenantDirectory + "/" + filename);
        if (!Files.isRegularFile(path)) {
            throw BusinessException.notFound("public_profile_media_not_found", "Imagem não encontrada.");
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            DetectedImage image = detect(bytes);
            Instant now = clock.instant();
            mediaRepository.save(new PublicProfileStoredMedia(UUID.randomUUID(), tenantId, kind, filename,
                    image.contentType(), bytes, bytes.length, now, now));
            return new MediaContent(bytes, image.contentType());
        } catch (IOException exception) {
            throw storageFailure(exception);
        }
    }

    @Override
    @Transactional
    public void deleteTenantFiles(UUID tenantId) {
        mediaRepository.deleteAllByTenantId(tenantId);
        Path directory = resolve(tenantId.toString());
        if (!Files.exists(directory)) return;
        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw storageFailure(exception);
                }
            });
        } catch (IOException exception) {
            throw storageFailure(exception);
        }
    }

    private Tenant required(UUID tenantId) {
        return tenants.findById(tenantId).orElseThrow(() ->
                BusinessException.notFound("tenant_not_found", "Empresa não encontrada."));
    }

    private String pathOf(Tenant tenant, PublicProfileMediaKind kind) {
        return switch (kind) {
            case BRAND -> tenant.logoUrl();
            case PROFILE -> tenant.publicProfileImagePath();
            case LOGO -> tenant.publicLogoPath();
            case BACKGROUND -> tenant.publicBackgroundImagePath();
        };
    }

    private Tenant withPath(Tenant tenant, PublicProfileMediaKind kind, String path, Instant at) {
        return switch (kind) {
            case BRAND -> tenant.withBrandLogo(path, at);
            case PROFILE -> tenant.withPublicMedia(path, tenant.publicLogoPath(),
                    tenant.publicBackgroundImagePath(), at);
            case LOGO -> tenant.withPublicMedia(tenant.publicProfileImagePath(), path,
                    tenant.publicBackgroundImagePath(), at);
            case BACKGROUND -> tenant.withPublicMedia(tenant.publicProfileImagePath(), tenant.publicLogoPath(),
                    path, at);
        };
    }

    private PublicProfileMediaKind kindOf(String filename) {
        if (filename.startsWith("brand-")) return PublicProfileMediaKind.BRAND;
        if (filename.startsWith("profile-")) return PublicProfileMediaKind.PROFILE;
        if (filename.startsWith("logo-")) return PublicProfileMediaKind.LOGO;
        return PublicProfileMediaKind.BACKGROUND;
    }

    private String updatedEvent(PublicProfileMediaKind kind) {
        return kind == PublicProfileMediaKind.BRAND ? "BRAND_LOGO_UPDATED" : "PUBLIC_PROFILE_MEDIA_UPDATED";
    }

    private String removedEvent(PublicProfileMediaKind kind) {
        return kind == PublicProfileMediaKind.BRAND ? "BRAND_LOGO_REMOVED" : "PUBLIC_PROFILE_MEDIA_REMOVED";
    }

    private Path resolve(String relativePath) {
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw BusinessException.notFound("public_profile_media_not_found", "Imagem não encontrada.");
        }
        return resolved;
    }

    private void deleteQuietly(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        if (!relativePath.matches("[0-9a-fA-F-]{36}/(?:brand|profile|logo|background)-"
                + "[0-9a-fA-F-]{36}\\.(?:png|jpg|webp)")) return;
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException ignored) {
            // O novo caminho já foi salvo. Uma falha de limpeza não deve desfazer a atualização do perfil.
        }
    }

    private DetectedImage detect(byte[] bytes) {
        if (bytes.length >= 8 && bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E
                && bytes[3] == 0x47 && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A
                && bytes[7] == 0x0A) {
            return new DetectedImage("png", "image/png");
        }
        if (bytes.length >= 3 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8
                && bytes[2] == (byte) 0xFF) {
            return new DetectedImage("jpg", "image/jpeg");
        }
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return new DetectedImage("webp", "image/webp");
        }
        throw BusinessException.badRequest("public_profile_media_type_invalid",
                "Envie uma imagem PNG, JPG ou WebP.");
    }

    private BusinessException storageFailure(Exception exception) {
        return new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "public_profile_media_storage_failed",
                "Não foi possível salvar ou carregar a imagem.");
    }

    private record DetectedImage(String extension, String contentType) {
    }
}
