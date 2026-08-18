package br.com.ares.shared.application;

import java.util.Map;
import java.util.UUID;

public interface AuditLogPort {
    void record(UUID tenantId, UUID actorId, String action, String resourceType,
                String resourceId, Map<String, Object> details);
}
