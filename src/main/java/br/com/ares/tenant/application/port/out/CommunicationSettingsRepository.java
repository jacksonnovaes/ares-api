package br.com.ares.tenant.application.port.out;

import br.com.ares.tenant.domain.model.CommunicationSettings;

import java.util.Optional;
import java.util.UUID;

public interface CommunicationSettingsRepository {

    Optional<CommunicationSettings> findByTenantId(UUID tenantId);

    CommunicationSettings save(CommunicationSettings settings);
}

