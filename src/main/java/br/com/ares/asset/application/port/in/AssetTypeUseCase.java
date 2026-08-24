package br.com.ares.asset.application.port.in;

import br.com.ares.asset.domain.model.AssetType;

import java.util.List;

public interface AssetTypeUseCase {

    AssetType create(CreateAssetTypeCommand command);

    List<AssetType> list();

    record CreateAssetTypeCommand(String name) {
    }
}
