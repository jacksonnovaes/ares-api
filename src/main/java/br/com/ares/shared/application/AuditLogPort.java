package br.com.ares.shared.application;

import java.util.Map;
import java.util.List;
import java.time.Instant;
import java.util.UUID;

public interface AuditLogPort {
    void record(UUID tenantId, UUID actorId, String action, String resourceType,
                String resourceId, Map<String, Object> details);

    List<AuditEventView> findAllByTenantId(UUID tenantId);

    record AuditEventView(UUID id, UUID actorId, String action, String resourceType,
                          String resourceId, String detailsJson, Instant occurredAt) {
    }
}
