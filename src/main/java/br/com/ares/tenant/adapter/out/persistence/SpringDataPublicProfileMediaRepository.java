package br.com.ares.tenant.adapter.out.persistence;

import br.com.ares.tenant.domain.model.PublicProfileMediaKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataPublicProfileMediaRepository extends JpaRepository<PublicProfileMediaJpaEntity, UUID> {
    Optional<PublicProfileMediaJpaEntity> findByTenantIdAndKind(UUID tenantId, PublicProfileMediaKind kind);

    void deleteByTenantIdAndKind(UUID tenantId, PublicProfileMediaKind kind);

    void deleteAllByTenantId(UUID tenantId);
}

