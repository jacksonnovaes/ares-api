package br.com.ares.asset.application.port.in;

import br.com.ares.asset.application.port.in.command.CreateAssetCommand;
import br.com.ares.asset.domain.model.Asset;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AssetUseCase {
    Asset create(CreateAssetCommand command);

    Asset get(UUID id);

    List<Asset> list(UUID customerId);


}
