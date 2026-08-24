package br.com.ares.serviceorder.application.port.in;

import br.com.ares.serviceorder.domain.model.ServiceOrderStatusDefinition;

import java.util.UUID;

public interface ServiceOrderStatusDirectory {
    ServiceOrderStatusDefinition requiredActive(UUID tenantId, String code);
    ServiceOrderStatusDefinition required(UUID tenantId, String code);
    void provisionDefaults(UUID tenantId);
}
