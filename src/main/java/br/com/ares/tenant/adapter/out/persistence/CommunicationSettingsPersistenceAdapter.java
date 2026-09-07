package br.com.ares.tenant.adapter.out.persistence;

import br.com.ares.tenant.application.port.out.CommunicationSettingsRepository;
import br.com.ares.tenant.domain.model.CommunicationSettings;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class CommunicationSettingsPersistenceAdapter implements CommunicationSettingsRepository {

    private final SpringDataCommunicationSettingsRepository repository;

    CommunicationSettingsPersistenceAdapter(SpringDataCommunicationSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CommunicationSettings> findByTenantId(UUID tenantId) {
        return repository.findById(tenantId).map(this::toDomain);
    }

    @Override
    public CommunicationSettings save(CommunicationSettings settings) {
        var entity = new CommunicationSettingsJpaEntity();
        entity.tenantId = settings.tenantId();
        entity.whatsappEnabled = settings.whatsappEnabled();
        entity.whatsappNumber = settings.whatsappNumber();
        entity.whatsappToken = settings.whatsappToken();
        entity.smtpEnabled = settings.smtpEnabled();
        entity.smtpHost = settings.smtpHost();
        entity.smtpPort = settings.smtpPort();
        entity.smtpUsername = settings.smtpUsername();
        entity.smtpPassword = settings.smtpPassword();
        entity.smtpFromEmail = settings.smtpFromEmail();
        entity.smtpStartTls = settings.smtpStartTls();
        entity.updatedAt = settings.updatedAt();
        return toDomain(repository.save(entity));
    }

    private CommunicationSettings toDomain(CommunicationSettingsJpaEntity entity) {
        return new CommunicationSettings(entity.tenantId, entity.whatsappEnabled, entity.whatsappNumber,
                entity.whatsappToken, entity.smtpEnabled, entity.smtpHost, entity.smtpPort,
                entity.smtpUsername, entity.smtpPassword, entity.smtpFromEmail, entity.smtpStartTls,
                entity.updatedAt);
    }
}

