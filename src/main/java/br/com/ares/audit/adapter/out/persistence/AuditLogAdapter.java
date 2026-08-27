package br.com.ares.audit.adapter.out.persistence;

import br.com.ares.shared.application.AuditLogPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
class AuditLogAdapter implements AuditLogPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogAdapter.class);

    private final SpringDataAuditEventRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    AuditLogAdapter(SpringDataAuditEventRepository repository, ObjectMapper objectMapper, Clock clock) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public void record(UUID tenantId, UUID actorId, String action, String resourceType,
                       String resourceId, Map<String, Object> details) {
        var entity = new AuditEventJpaEntity();
        entity.id = UUID.randomUUID();
        entity.tenantId = tenantId;
        entity.actorId = actorId;
        entity.action = action;
        entity.resourceType = resourceType;
        entity.resourceId = resourceId;
        entity.detailsJson = json(details);
        entity.occurredAt = clock.instant();
        repository.save(entity);
    }

    @Override
    public List<AuditEventView> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantIdOrderByOccurredAtAsc(tenantId).stream()
                .map(entity -> new AuditEventView(entity.id, entity.actorId, entity.action,
                        entity.resourceType, entity.resourceId, entity.detailsJson, entity.occurredAt))
                .toList();
    }

    private String json(Map<String, Object> details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException exception) {
            LOGGER.warn("Could not serialize audit details", exception);
            return "{}";
        }
    }
}
