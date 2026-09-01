package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.PublicProfileMediaUseCase;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.PublicProfileMediaKind;
import br.com.ares.tenant.domain.model.Tenant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import java.util.Comparator;

@Service
public class PublicProfileMediaService implements PublicProfileMediaUseCase {

    static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final TenantRepository tenants;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;
    private final Path root;

    public PublicProfileMediaService(TenantRepository tenants, CurrentActorProvider currentActor, AuditLogPort audit,
                                     Clock clock,
                                     @Value("${ares.storage.public-profile-media-root:./data/public-profile-media}")
                                     String storageRoot) {
        this.tenants = tenants;
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
        Path destination = resolve(relativePath);
        String previousPath = kind == PublicProfileMediaKind.LOGO
                ? tenant.publicLogoPath() : tenant.publicBackgroundImagePath();
        try {
            Files.createDirectories(destination.getParent());
            Files.write(destination, content, StandardOpenOption.CREATE_NEW);
            Tenant updated = kind == PublicProfileMediaKind.LOGO
                    ? tenant.withPublicMedia(relativePath, tenant.publicBackgroundImagePath(), clock.instant())
                    : tenant.withPublicMedia(tenant.publicLogoPath(), relativePath, clock.instant());
            try {
                tenants.save(updated);
            } catch (RuntimeException exception) {
                Files.deleteIfExists(destination);
                throw exception;
            }
            deleteQuietly(previousPath);
            audit.record(actor.tenantId(), actor.userId(), "PUBLIC_PROFILE_MEDIA_UPDATED", "TENANT",
                    actor.tenantId().toString(), Map.of("kind", kind.name(), "contentType", image.contentType()));
            return new StoredMedia(kind, relativePath);
        } catch (IOException exception) {
            throw storageFailure(exception);
        }
    }

    @Override
    @Transactional
    public void remove(PublicProfileMediaKind kind) {
        var actor = currentActor.requiredActor();
        Tenant tenant = required(actor.tenantId());
        String previousPath = kind == PublicProfileMediaKind.LOGO
                ? tenant.publicLogoPath() : tenant.publicBackgroundImagePath();
        Tenant updated = kind == PublicProfileMediaKind.LOGO
                ? tenant.withPublicMedia(null, tenant.publicBackgroundImagePath(), clock.instant())
                : tenant.withPublicMedia(tenant.publicLogoPath(), null, clock.instant());
        tenants.save(updated);
        deleteQuietly(previousPath);
        audit.record(actor.tenantId(), actor.userId(), "PUBLIC_PROFILE_MEDIA_REMOVED", "TENANT",
                actor.tenantId().toString(), Map.of("kind", kind.name()));
    }

    @Override
    @Transactional(readOnly = true)
    public MediaContent load(String tenantDirectory, String filename) {
        if (!tenantDirectory.matches("[0-9a-fA-F-]{36}")
                || !filename.matches("(?:logo|background)-[0-9a-fA-F-]{36}\\.(?:png|jpg|webp)")) {
            throw BusinessException.notFound("public_profile_media_not_found", "Imagem não encontrada.");
        }
        Path path = resolve(tenantDirectory + "/" + filename);
        if (!Files.isRegularFile(path)) {
            throw BusinessException.notFound("public_profile_media_not_found", "Imagem não encontrada.");
        }
        try {
            return new MediaContent(Files.readAllBytes(path), contentType(filename));
        } catch (IOException exception) {
            throw storageFailure(exception);
        }
    }

    @Override
    public void deleteTenantFiles(UUID tenantId) {
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

    private Path resolve(String relativePath) {
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw BusinessException.notFound("public_profile_media_not_found", "Imagem não encontrada.");
        }
        return resolved;
    }

    private void deleteQuietly(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
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

    private String contentType(String filename) {
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private BusinessException storageFailure(Exception exception) {
        return new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "public_profile_media_storage_failed",
                "Não foi possível salvar ou carregar a imagem no servidor.");
    }

    private record DetectedImage(String extension, String contentType) {
    }
}
