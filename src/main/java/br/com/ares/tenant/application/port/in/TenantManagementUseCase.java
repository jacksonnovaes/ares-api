package br.com.ares.tenant.application.port.in;

import br.com.ares.tenant.domain.model.Tenant;
import br.com.ares.tenant.domain.model.TenantStatus;

import java.util.Optional;
import java.util.UUID;

public interface TenantManagementUseCase {

    Tenant create(CreateTenantCommand command);

    Tenant requiredById(UUID id);

    Optional<Tenant> findBySlug(String slug);

    Tenant changeStatus(UUID id, TenantStatus status);

    record CreateTenantCommand(String legalName, String tradeName, String slug, String document,
                               String logoUrl, String primaryColor) {
    }
}
