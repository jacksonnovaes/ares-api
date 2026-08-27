package br.com.ares.asset.application.service;

import br.com.ares.asset.application.port.in.AssetTypeUseCase;
import br.com.ares.asset.application.port.out.AssetTypeRepository;
import br.com.ares.asset.domain.model.AssetType;
import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetTypeServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-23T15:00:00Z");

    private UUID tenantId;
    private InMemoryAssetTypeRepository repository;
    private AssetTypeService service;
    private RecordingAudit audit;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), tenantId, "admin@example.com",
                Set.of("ADMIN"), Set.of("ASSET_CREATE", "ASSET_READ"), null);
        repository = new InMemoryAssetTypeRepository();
        audit = new RecordingAudit();
        CurrentActorProvider currentActor = () -> actor;
        service = new AssetTypeService(repository, currentActor, audit, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void provisionsTheSixDefaultTypesForEachTenant() {
        service.provisionDefaults(tenantId);
        service.provisionDefaults(tenantId);

        assertThat(service.list())
                .extracting(AssetType::code)
                .containsExactlyInAnyOrder("VEHICLE", "PHONE", "COMPUTER", "EQUIPMENT", "PROPERTY", "OTHER");
        assertThat(service.list()).allMatch(AssetType::systemDefault);
    }

    @Test
    void createsACustomTypeWithAStableNormalizedCode() {
        service.provisionDefaults(tenantId);

        AssetType created = service.create(new AssetTypeUseCase.CreateAssetTypeCommand("  Impressora 3D  "));

        assertThat(created.code()).isEqualTo("IMPRESSORA_3D");
        assertThat(created.name()).isEqualTo("Impressora 3D");
        assertThat(created.systemDefault()).isFalse();
        assertThat(created.active()).isTrue();
        assertThat(audit.actions).containsExactly("ASSET_TYPE_CREATED");
    }

    @Test
    void rejectsDuplicateNamesIgnoringCase() {
        service.create(new AssetTypeUseCase.CreateAssetTypeCommand("Drone"));

        assertThatThrownBy(() -> service.create(new AssetTypeUseCase.CreateAssetTypeCommand("drone")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe");
    }

    private static final class RecordingAudit implements AuditLogPort {
        private final List<String> actions = new ArrayList<>();

        @Override
        public void record(UUID tenantId, UUID actorId, String action, String resourceType,
                           String resourceId, Map<String, Object> details) {
            actions.add(action);
        }

        @Override
        public List<AuditEventView> findAllByTenantId(UUID tenantId) {
            return List.of();
        }
    }

    private static final class InMemoryAssetTypeRepository implements AssetTypeRepository {
        private final Map<UUID, AssetType> values = new HashMap<>();

        @Override
        public AssetType save(AssetType assetType) {
            values.put(assetType.id(), assetType);
            return assetType;
        }

        @Override
        public List<AssetType> findAllByTenantId(UUID tenantId) {
            return values.values().stream()
                    .filter(value -> value.tenantId().equals(tenantId))
                    .sorted(Comparator.comparing(AssetType::name))
                    .toList();
        }

        @Override
        public Optional<AssetType> findByTenantIdAndCode(UUID tenantId, String code) {
            return values.values().stream()
                    .filter(value -> value.tenantId().equals(tenantId) && value.code().equals(code))
                    .findFirst();
        }

        @Override
        public boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name) {
            return values.values().stream()
                    .anyMatch(value -> value.tenantId().equals(tenantId) && value.name().equalsIgnoreCase(name));
        }
    }
}
