package br.com.ares.serviceorder.application.port.in;

import br.com.ares.serviceorder.domain.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public interface ServiceOrderUseCase {
    ServiceOrder create(CreateOrderCommand command);
    ServiceOrder get(UUID id);
    List<ServiceOrder> list();
    ServiceOrder changeStatus(UUID id, ChangeStatusCommand command);
    record CreateOrderCommand(UUID customerId, UUID assetId, Set<UUID> serviceIds, String title,
                              String description, ServiceOrderPriority priority, BigDecimal estimatedValue,
                              UUID assignedTechnicianId, Instant dueAt) {}
    record ChangeStatusCommand(ServiceOrderStatus status, BigDecimal finalValue) {}
}
