package br.com.ares.serviceorder.application.port.out;

import br.com.ares.serviceorder.domain.model.ServiceOrder;
import java.util.*;

public interface ServiceOrderRepository {
    ServiceOrder save(ServiceOrder order);
    Optional<ServiceOrder> findByIdAndTenantId(UUID id,UUID tenantId);
    List<ServiceOrder> findAllByTenantId(UUID tenantId);
    List<ServiceOrder> findAllByTenantIdAndCustomerId(UUID tenantId,UUID customerId);
    List<ServiceOrder> findAllByTenantIdAndAssignedTechnicianId(UUID tenantId,UUID technicianId);
}
