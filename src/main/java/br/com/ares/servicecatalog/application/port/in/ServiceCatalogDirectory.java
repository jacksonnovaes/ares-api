package br.com.ares.servicecatalog.application.port.in;

import java.util.Set;
import java.util.UUID;

public interface ServiceCatalogDirectory {
    boolean allExistAndActive(UUID tenantId, Set<UUID> serviceIds);
}
