package br.com.ares.serviceorder.application.port.out;

import br.com.ares.serviceorder.domain.model.ServiceOrderStatusDefinition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderStatusRepository {
    ServiceOrderStatusDefinition save(ServiceOrderStatusDefinition status);
    Optional<ServiceOrderStatusDefinition> findByTenantIdAndCode(UUID tenantId, String code);
    List<ServiceOrderStatusDefinition> findAllByTenantId(UUID tenantId);
    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);
}
