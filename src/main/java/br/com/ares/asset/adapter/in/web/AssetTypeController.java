package br.com.ares.asset.adapter.in.web;

import br.com.ares.asset.adapter.in.web.request.CreateAssetTypeRequest;
import br.com.ares.asset.application.port.in.AssetTypeUseCase;
import br.com.ares.asset.domain.model.AssetType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-types")
public class AssetTypeController {

    private final AssetTypeUseCase assetTypes;

    public AssetTypeController(AssetTypeUseCase assetTypes) {
        this.assetTypes = assetTypes;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET_READ')")
    public List<AssetType> list() {
        return assetTypes.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ASSET_CREATE') and !hasRole('CUSTOMER')")
    public AssetType create(@Valid @RequestBody CreateAssetTypeRequest request) {
        return assetTypes.create(new AssetTypeUseCase.CreateAssetTypeCommand(request.name()));
    }
}
