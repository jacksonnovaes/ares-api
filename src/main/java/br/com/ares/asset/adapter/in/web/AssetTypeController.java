package br.com.ares.asset.adapter.in.web;

import br.com.ares.asset.application.port.in.AssetTypeUseCase;
import br.com.ares.asset.domain.model.AssetType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    List<AssetType> list() {
        return assetTypes.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ASSET_CREATE') and !hasRole('CUSTOMER')")
    AssetType create(@Valid @RequestBody CreateAssetTypeRequest request) {
        return assetTypes.create(new AssetTypeUseCase.CreateAssetTypeCommand(request.name()));
    }

    record CreateAssetTypeRequest(@NotBlank @Size(max = 100) String name) {
    }
}
