package br.com.ares.servicecatalog.application.port.in;

import br.com.ares.servicecatalog.domain.model.CatalogService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ServiceCatalogDirectory {
    boolean allExistAndActive(UUID tenantId, Set<UUID> serviceIds);
    boolean anyRequiresAsset(UUID tenantId, Set<UUID> serviceIds);
    List<CatalogService> listActive(UUID tenantId);
}
