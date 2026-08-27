package br.com.ares.tenant.application.port.out;

import br.com.ares.tenant.domain.model.Tenant;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository {
    Tenant save(Tenant tenant);
    Optional<Tenant> findById(UUID id);
    Optional<Tenant> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByDocument(String document);
    void deleteAllData(UUID tenantId);
}
