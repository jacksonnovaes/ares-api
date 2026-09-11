package br.com.ares.identity.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantUserDirectory {
    boolean activeUserExists(UUID tenantId, UUID userId);
    boolean activeTechnicianExists(UUID tenantId, UUID userId);
    List<TenantUser> activeTechnicians(UUID tenantId);
    Optional<String> userName(UUID tenantId, UUID userId);

    record TenantUser(UUID id, String name) {
    }
}
