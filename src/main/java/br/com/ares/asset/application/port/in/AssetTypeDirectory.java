package br.com.ares.asset.application.port.in;

import br.com.ares.asset.domain.model.AssetType;

import java.util.UUID;

public interface AssetTypeDirectory {

    AssetType required(UUID tenantId, String code);

    AssetType requiredActive(UUID tenantId, String code);

    void provisionDefaults(UUID tenantId);
}
