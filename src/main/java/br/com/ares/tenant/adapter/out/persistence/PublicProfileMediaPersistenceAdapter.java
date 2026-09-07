package br.com.ares.tenant.adapter.out.persistence;

import br.com.ares.tenant.application.port.out.PublicProfileMediaRepository;
import br.com.ares.tenant.domain.model.PublicProfileMediaKind;
import br.com.ares.tenant.domain.model.PublicProfileStoredMedia;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class PublicProfileMediaPersistenceAdapter implements PublicProfileMediaRepository {

    private final SpringDataPublicProfileMediaRepository repository;

    PublicProfileMediaPersistenceAdapter(SpringDataPublicProfileMediaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PublicProfileStoredMedia save(PublicProfileStoredMedia media) {
        var entity = new PublicProfileMediaJpaEntity();
        entity.id = media.id();
        entity.tenantId = media.tenantId();
        entity.kind = media.kind();
        entity.filename = media.filename();
        entity.contentType = media.contentType();
        entity.content = media.content();
        entity.sizeBytes = media.sizeBytes();
        entity.createdAt = media.createdAt();
        entity.updatedAt = media.updatedAt();
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<PublicProfileStoredMedia> findByTenantIdAndKind(UUID tenantId, PublicProfileMediaKind kind) {
        return repository.findByTenantIdAndKind(tenantId, kind).map(this::toDomain);
    }

    @Override
    public void deleteByTenantIdAndKind(UUID tenantId, PublicProfileMediaKind kind) {
        repository.deleteByTenantIdAndKind(tenantId, kind);
    }

    @Override
    public void deleteAllByTenantId(UUID tenantId) {
        repository.deleteAllByTenantId(tenantId);
    }

    private PublicProfileStoredMedia toDomain(PublicProfileMediaJpaEntity entity) {
        return new PublicProfileStoredMedia(entity.id, entity.tenantId, entity.kind, entity.filename,
                entity.contentType, entity.content, entity.sizeBytes, entity.createdAt, entity.updatedAt);
    }
}

