package br.com.ares.tenant.application.port.out;

import br.com.ares.tenant.domain.model.PublicProfileMediaKind;
import br.com.ares.tenant.domain.model.PublicProfileStoredMedia;

import java.util.Optional;
import java.util.UUID;

public interface PublicProfileMediaRepository {

    PublicProfileStoredMedia save(PublicProfileStoredMedia media);

    Optional<PublicProfileStoredMedia> findByTenantIdAndKind(UUID tenantId, PublicProfileMediaKind kind);

    void deleteByTenantIdAndKind(UUID tenantId, PublicProfileMediaKind kind);

    void deleteAllByTenantId(UUID tenantId);
}

