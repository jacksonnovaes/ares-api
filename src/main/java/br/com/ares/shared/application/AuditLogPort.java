package br.com.ares.shared.application;

import java.util.Map;
import java.util.List;
import java.time.Instant;
import java.util.UUID;

public interface AuditLogPort {
    void record(UUID tenantId, UUID actorId, String action, String resourceType,
                String resourceId, Map<String, Object> details);

    List<AuditEventView> findAllByTenantId(UUID tenantId);

    default List<AuditEventView> findAllByTenantIdAndResource(UUID tenantId, String resourceType, String resourceId) {
        return findAllByTenantId(tenantId).stream()
                .filter(event -> resourceType.equals(event.resourceType()) && resourceId.equals(event.resourceId()))
                .toList();
    }

    record AuditEventView(UUID id, UUID actorId, String action, String resourceType,
                          String resourceId, String detailsJson, Instant occurredAt) {
    }
}
