package br.com.ares.asset.adapter.out.persistence;

import br.com.ares.asset.application.port.out.AssetTypeRepository;
import br.com.ares.asset.domain.model.AssetType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class AssetTypePersistenceAdapter implements AssetTypeRepository {

    private final SpringDataAssetTypeRepository repository;

    AssetTypePersistenceAdapter(SpringDataAssetTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public AssetType save(AssetType value) {
        return toDomain(repository.save(toEntity(value)));
    }

    @Override
    public List<AssetType> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantIdOrderBySystemDefaultDescNameAsc(tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<AssetType> findByTenantIdAndCode(UUID tenantId, String code) {
        return repository.findByTenantIdAndCode(tenantId, code).map(this::toDomain);
    }

    @Override
    public boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name) {
        return repository.existsByTenantIdAndNameIgnoreCase(tenantId, name);
    }

    private AssetTypeJpaEntity toEntity(AssetType value) {
        var entity = new AssetTypeJpaEntity();
        entity.id = value.id();
        entity.tenantId = value.tenantId();
        entity.code = value.code();
        entity.name = value.name();
        entity.systemDefault = value.systemDefault();
        entity.active = value.active();
        entity.createdAt = value.createdAt();
        entity.updatedAt = value.updatedAt();
        return entity;
    }

    private AssetType toDomain(AssetTypeJpaEntity entity) {
        return new AssetType(entity.id, entity.tenantId, entity.code, entity.name, entity.systemDefault,
                entity.active, entity.createdAt, entity.updatedAt);
    }
}
