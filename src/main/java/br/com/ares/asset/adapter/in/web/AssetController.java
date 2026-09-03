package br.com.ares.asset.adapter.in.web;

import br.com.ares.asset.adapter.in.web.request.CreateAssetRequest;
import br.com.ares.asset.application.port.in.AssetUseCase;
import br.com.ares.asset.application.port.in.command.CreateAssetCommand;
import br.com.ares.asset.domain.model.Asset;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {
    private final AssetUseCase assets;

    public AssetController(AssetUseCase assets) {
        this.assets = assets;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ASSET_CREATE')")
    Asset create(@Valid @RequestBody CreateAssetRequest r) {
        return assets.create(new CreateAssetCommand(r.customerId(), r.type(), r.name(), r.brand(),
                r.model(), r.serialNumber(), r.attributes()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET_READ')")
    List<Asset> list(@RequestParam(required = false) UUID customerId) {
        return assets.list(customerId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_READ')")
    Asset get(@PathVariable UUID id) {
        return assets.get(id);
    }


}
