package br.com.ares.serviceorder.application.service;

import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusUseCase;
import br.com.ares.serviceorder.application.port.out.ServiceOrderStatusRepository;
import br.com.ares.serviceorder.domain.model.ServiceOrderStatusDefinition;
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
public class ServiceOrderStatusService implements ServiceOrderStatusUseCase, ServiceOrderStatusDirectory {

    private static final int MAX_CODE_LENGTH = 50;
    private static final List<DefaultStatus> DEFAULTS = List.of(
            new DefaultStatus("OPEN", "Aberto", 10),
            new DefaultStatus("ANALYSIS", "Em análise", 20),
            new DefaultStatus("EXECUTION", "Execução", 30),
            new DefaultStatus("BLOCKED", "Bloqueada", 40),
            new DefaultStatus("COMPLETED", "Concluída", 90)
    );

    private final ServiceOrderStatusRepository repository;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public ServiceOrderStatusService(ServiceOrderStatusRepository repository, CurrentActorProvider currentActor,
                                     AuditLogPort audit, Clock clock) {
        this.repository = repository;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ServiceOrderStatusDefinition create(CreateStatusCommand command) {
        var actor = currentActor.requiredActor();
        String name = normalizeName(command.name());
        if (repository.existsByTenantIdAndNameIgnoreCase(actor.tenantId(), name)) {
            throw BusinessException.conflict("service_order_status_name_exists",
                    "Já existe um status de ordem com este nome.");
        }
        List<ServiceOrderStatusDefinition> current = repository.findAllByTenantId(actor.tenantId());
        int displayOrder = current.stream().mapToInt(ServiceOrderStatusDefinition::displayOrder).max().orElse(0) + 10;
        Instant now = clock.instant();
        var value = repository.save(new ServiceOrderStatusDefinition(UUID.randomUUID(), actor.tenantId(),
                availableCode(actor.tenantId(), name), name, false, true, displayOrder, now, now));
        audit.record(actor.tenantId(), actor.userId(), "SERVICE_ORDER_STATUS_CREATED", "SERVICE_ORDER_STATUS",
                value.code(), Map.of("name", value.name()));
        return value;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOrderStatusDefinition> list() {
        return repository.findAllByTenantId(currentActor.requiredActor().tenantId()).stream()
                .filter(ServiceOrderStatusDefinition::active)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOrderStatusDefinition requiredActive(UUID tenantId, String code) {
        ServiceOrderStatusDefinition value = required(tenantId, code);
        if (!value.active()) {
            throw BusinessException.badRequest("service_order_status_inactive", "O status informado está inativo.");
        }
        return value;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOrderStatusDefinition required(UUID tenantId, String code) {
        return repository.findByTenantIdAndCode(tenantId, normalizeCode(code)).orElseThrow(() ->
                BusinessException.badRequest("service_order_status_not_found", "O status informado não existe."));
    }

    @Override
    @Transactional
    public void provisionDefaults(UUID tenantId) {
        Instant now = clock.instant();
        for (DefaultStatus value : DEFAULTS) {
            if (repository.findByTenantIdAndCode(tenantId, value.code()).isEmpty()) {
                repository.save(new ServiceOrderStatusDefinition(UUID.randomUUID(), tenantId, value.code(),
                        value.name(), true, true, value.displayOrder(), now, now));
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
        if (base.isBlank()) base = "CUSTOM_STATUS";
        base = base.substring(0, Math.min(base.length(), MAX_CODE_LENGTH));
        String candidate = base;
        int suffix = 2;
        while (repository.findByTenantIdAndCode(tenantId, candidate).isPresent()) {
            String ending = "_" + suffix++;
            candidate = base.substring(0, Math.min(base.length(), MAX_CODE_LENGTH - ending.length())) + ending;
        }
        return candidate;
    }

    private record DefaultStatus(String code, String name, int displayOrder) {
    }
}
