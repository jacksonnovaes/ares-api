package br.com.ares.asset.adapter.out.persistence;

import br.com.ares.asset.adapter.out.persistence.mapper.AssetMapper;
import br.com.ares.asset.application.port.out.AssetRepository;
import br.com.ares.asset.domain.model.Asset;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static br.com.ares.asset.adapter.out.persistence.mapper.AssetMapper.toDomain;
import static br.com.ares.asset.adapter.out.persistence.mapper.AssetMapper.toEntity;

@Component
class AssetPersistenceAdapter implements AssetRepository {
    private final SpringDataAssetRepository repository;
    private final ObjectMapper objectMapper;

    AssetPersistenceAdapter(SpringDataAssetRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Asset save(Asset value) {
        return toDomain(repository.save(toEntity(value)));
    }

    @Override
    public Optional<Asset> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(AssetMapper::toDomain);
    }

    @Override
    public List<Asset> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantIdOrderByNameAsc(tenantId).stream().map(AssetMapper::toDomain).toList();
    }

    @Override
    public List<Asset> findAllByTenantIdAndCustomerId(UUID tenantId, UUID customerId) {
        return repository.findAllByTenantIdAndCustomerIdOrderByNameAsc(tenantId, customerId).stream()
                .map(AssetMapper::toDomain).toList();
    }

    @Override
    public boolean existsByIdAndTenantIdAndCustomerId(UUID id, UUID tenantId, UUID customerId) {
        return repository.existsByIdAndTenantIdAndCustomerId(id, tenantId, customerId);
    }
}
