package br.com.ares.asset.application.port.in;

import br.com.ares.asset.domain.model.Asset;
import br.com.ares.asset.domain.model.AssetType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AssetUseCase {
    Asset create(CreateAssetCommand command);

    Asset get(UUID id);

    List<Asset> list(UUID customerId);

    record CreateAssetCommand(UUID customerId, AssetType type, String name, String brand,
                              String model, String serialNumber, Map<String, String> attributes) {
    }
}
