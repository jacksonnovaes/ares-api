package br.com.ares.asset.application.service;

import br.com.ares.asset.application.port.in.AssetDirectory;
import br.com.ares.asset.application.port.in.AssetUseCase;
import br.com.ares.asset.application.port.out.AssetRepository;
import br.com.ares.asset.domain.model.Asset;
import br.com.ares.customer.application.port.in.CustomerDirectory;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AssetService implements AssetUseCase, AssetDirectory {
    private final AssetRepository repository;
    private final CustomerDirectory customers;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public AssetService(AssetRepository repository, CustomerDirectory customers,
                        CurrentActorProvider currentActor, AuditLogPort audit, Clock clock) {
        this.repository = repository;
        this.customers = customers;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Asset create(CreateAssetCommand command) {
        var actor = currentActor.requiredActor();
        if (!customers.exists(actor.tenantId(), command.customerId())) {
            throw BusinessException.notFound("customer_not_found", "Cliente não encontrado.");
        }
        Instant now = clock.instant();
        var asset = new Asset(UUID.randomUUID(), actor.tenantId(), command.customerId(), command.type(),
                command.name().trim(), command.brand(), command.model(), command.serialNumber(),
                command.attributes() == null ? Map.of() : Map.copyOf(command.attributes()), now, now);
        asset = repository.save(asset);
        audit.record(actor.tenantId(), actor.userId(), "ASSET_CREATED", "ASSET", asset.id().toString(),
                Map.of("customerId", command.customerId()));
        return asset;
    }

    @Override
    @Transactional(readOnly = true)
    public Asset get(UUID id) {
        var actor = currentActor.requiredActor();
        Asset asset = required(id, actor.tenantId());
        if (actor.hasRole("CUSTOMER") && !asset.customerId().equals(actor.customerId())) {
            throw BusinessException.notFound("asset_not_found", "Ativo não encontrado.");
        }
        return asset;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> list(UUID customerId) {
        var actor = currentActor.requiredActor();
        UUID effectiveCustomerId = actor.hasRole("CUSTOMER") ? actor.customerId() : customerId;
        if (actor.hasRole("CUSTOMER") && effectiveCustomerId == null) return List.of();
        return effectiveCustomerId == null
                ? repository.findAllByTenantId(actor.tenantId())
                : repository.findAllByTenantIdAndCustomerId(actor.tenantId(), effectiveCustomerId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean belongsToCustomer(UUID tenantId, UUID assetId, UUID customerId) {
        return repository.existsByIdAndTenantIdAndCustomerId(assetId, tenantId, customerId);
    }

    private Asset required(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).orElseThrow(() ->
                BusinessException.notFound("asset_not_found", "Ativo não encontrado."));
    }
}
