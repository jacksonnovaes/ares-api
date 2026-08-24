package br.com.ares.asset.application.service;

import br.com.ares.asset.application.port.in.AssetTypeDirectory;
import br.com.ares.asset.application.port.in.AssetTypeUseCase;
import br.com.ares.asset.application.port.out.AssetTypeRepository;
import br.com.ares.asset.domain.model.AssetType;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AssetTypeService implements AssetTypeUseCase, AssetTypeDirectory {

    private static final int MAX_CODE_LENGTH = 50;
    private static final List<DefaultType> DEFAULT_TYPES = List.of(
            new DefaultType("VEHICLE", "Veículo"),
            new DefaultType("PHONE", "Telefone"),
            new DefaultType("COMPUTER", "Computador"),
            new DefaultType("EQUIPMENT", "Equipamento"),
            new DefaultType("PROPERTY", "Imóvel"),
            new DefaultType("OTHER", "Outro")
    );

    private final AssetTypeRepository repository;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public AssetTypeService(AssetTypeRepository repository, CurrentActorProvider currentActor,
                            AuditLogPort audit, Clock clock) {
        this.repository = repository;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AssetType create(CreateAssetTypeCommand command) {
        var actor = currentActor.requiredActor();
        String name = normalizeName(command.name());
        if (repository.existsByTenantIdAndNameIgnoreCase(actor.tenantId(), name)) {
            throw BusinessException.conflict("asset_type_name_exists", "Já existe um tipo de ativo com este nome.");
        }

        String code = availableCode(actor.tenantId(), name);
        Instant now = clock.instant();
        AssetType assetType = repository.save(new AssetType(UUID.randomUUID(), actor.tenantId(), code, name,
                false, true, now, now));
        audit.record(actor.tenantId(), actor.userId(), "ASSET_TYPE_CREATED", "ASSET_TYPE", code,
                Map.of("name", name));
        return assetType;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetType> list() {
        return repository.findAllByTenantId(currentActor.requiredActor().tenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public AssetType required(UUID tenantId, String code) {
        return repository.findByTenantIdAndCode(tenantId, normalizeCode(code)).orElseThrow(() ->
                BusinessException.badRequest("asset_type_not_found", "O tipo de ativo informado não existe."));
    }

    @Override
    @Transactional(readOnly = true)
    public AssetType requiredActive(UUID tenantId, String code) {
        AssetType assetType = required(tenantId, code);
        if (!assetType.active()) {
            throw BusinessException.badRequest("asset_type_inactive", "O tipo de ativo informado está inativo.");
        }
        return assetType;
    }

    @Override
    @Transactional
    public void provisionDefaults(UUID tenantId) {
        Instant now = clock.instant();
        for (DefaultType value : DEFAULT_TYPES) {
            if (repository.findByTenantIdAndCode(tenantId, value.code()).isEmpty()) {
                repository.save(new AssetType(UUID.randomUUID(), tenantId, value.code(), value.name(),
                        true, true, now, now));
            }
        }
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeCode(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String availableCode(UUID tenantId, String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (base.isBlank()) {
            base = "CUSTOM_TYPE";
        }
        base = base.substring(0, Math.min(base.length(), MAX_CODE_LENGTH));

        String candidate = base;
        int suffix = 2;
        while (repository.findByTenantIdAndCode(tenantId, candidate).isPresent()) {
            String ending = "_" + suffix++;
            candidate = base.substring(0, Math.min(base.length(), MAX_CODE_LENGTH - ending.length())) + ending;
        }
        return candidate;
    }

    private record DefaultType(String code, String name) {
    }
}
