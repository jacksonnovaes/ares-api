package br.com.ares.tenant.adapter.out.persistence;

import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.Tenant;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class TenantPersistenceAdapter implements TenantRepository {

    private final SpringDataTenantRepository repository;

    TenantPersistenceAdapter(SpringDataTenantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Tenant save(Tenant tenant) {
        return toDomain(repository.save(toEntity(tenant)));
    }

    @Override
    public Optional<Tenant> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Tenant> findBySlug(String slug) {
        return repository.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return repository.existsBySlug(slug);
    }

    @Override
    public boolean existsByDocument(String document) {
        return repository.existsByDocument(document.replaceAll("\\D", ""));
    }

    private TenantJpaEntity toEntity(Tenant tenant) {
        var entity = new TenantJpaEntity();
        entity.id = tenant.id();
        entity.legalName = tenant.legalName();
        entity.tradeName = tenant.tradeName();
        entity.slug = tenant.slug();
        entity.document = tenant.document();
        entity.status = tenant.status();
        entity.logoUrl = tenant.logoUrl();
        entity.primaryColor = tenant.primaryColor();
        entity.requireAssets = tenant.requireAssets();
        entity.createdAt = tenant.createdAt();
        entity.updatedAt = tenant.updatedAt();
        return entity;
    }

    private Tenant toDomain(TenantJpaEntity entity) {
        return new Tenant(entity.id, entity.legalName, entity.tradeName, entity.slug, entity.document,
                entity.status, entity.logoUrl, entity.primaryColor, entity.requireAssets, entity.createdAt,
                entity.updatedAt);
    }
}
