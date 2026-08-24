package br.com.ares.asset.adapter.out.persistence;

import br.com.ares.asset.application.port.out.AssetRepository;
import br.com.ares.asset.domain.model.Asset;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;

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
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public List<Asset> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantIdOrderByNameAsc(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Asset> findAllByTenantIdAndCustomerId(UUID tenantId, UUID customerId) {
        return repository.findAllByTenantIdAndCustomerIdOrderByNameAsc(tenantId, customerId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public boolean existsByIdAndTenantIdAndCustomerId(UUID id, UUID tenantId, UUID customerId) {
        return repository.existsByIdAndTenantIdAndCustomerId(id, tenantId, customerId);
    }

    private AssetJpaEntity toEntity(Asset v) {
        var e = new AssetJpaEntity();
        e.id = v.id();
        e.tenantId = v.tenantId();
        e.customerId = v.customerId();
        e.type = v.type();
        e.name = v.name();
        e.brand = v.brand();
        e.model = v.model();
        e.serialNumber = v.serialNumber();
        try {
            e.attributesJson = objectMapper.writeValueAsString(v.attributes());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid asset attributes", ex);
        }
        e.createdAt = v.createdAt();
        e.updatedAt = v.updatedAt();
        return e;
    }

    private Asset toDomain(AssetJpaEntity e) {
        try {
            Map<String, String> attributes = objectMapper.readValue(e.attributesJson, new TypeReference<>() {
            });
            return new Asset(e.id, e.tenantId, e.customerId, e.type, e.name, e.brand, e.model, e.serialNumber,
                    Map.copyOf(attributes), e.createdAt, e.updatedAt);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid stored asset attributes", ex);
        }
    }
}
