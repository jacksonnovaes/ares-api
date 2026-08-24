package br.com.ares.asset.application.port.out;

import br.com.ares.asset.domain.model.AssetType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetTypeRepository {

    AssetType save(AssetType assetType);

    List<AssetType> findAllByTenantId(UUID tenantId);

    Optional<AssetType> findByTenantIdAndCode(UUID tenantId, String code);

    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);
}
