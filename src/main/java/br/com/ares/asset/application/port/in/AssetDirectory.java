package br.com.ares.asset.application.port.in;

import java.util.UUID;

public interface AssetDirectory {
    boolean belongsToCustomer(UUID tenantId, UUID assetId, UUID customerId);
}
