package br.com.ares.identity.application.port.in;

import java.util.UUID;

public interface TenantUserDirectory {
    boolean activeUserExists(UUID tenantId, UUID userId);
}
