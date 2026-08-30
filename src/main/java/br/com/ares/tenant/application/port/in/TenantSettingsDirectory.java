package br.com.ares.tenant.application.port.in;

import java.util.UUID;

public interface TenantSettingsDirectory {
    boolean requireAssets(UUID tenantId);

    int subscriptionUserLimit(UUID tenantId);
}
