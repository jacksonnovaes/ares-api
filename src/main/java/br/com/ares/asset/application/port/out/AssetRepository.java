package br.com.ares.asset.application.port.out;

import br.com.ares.asset.domain.model.Asset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository {
    Asset save(Asset asset);
    Optional<Asset> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Asset> findAllByTenantId(UUID tenantId);
    List<Asset> findAllByTenantIdAndCustomerId(UUID tenantId, UUID customerId);
    boolean existsByIdAndTenantIdAndCustomerId(UUID id, UUID tenantId, UUID customerId);
}
