package br.com.ares.servicecatalog.application.port.out;

import br.com.ares.servicecatalog.domain.model.CatalogService;
import java.util.*;

public interface ServiceCatalogRepository {
    CatalogService save(CatalogService service);
    Optional<CatalogService> findByIdAndTenantId(UUID id, UUID tenantId);
    List<CatalogService> findAllByTenantId(UUID tenantId);
    long countActiveByTenantIdAndIds(UUID tenantId, Set<UUID> ids);
    boolean existsMaintenanceByTenantIdAndIds(UUID tenantId, Set<UUID> ids);
}
