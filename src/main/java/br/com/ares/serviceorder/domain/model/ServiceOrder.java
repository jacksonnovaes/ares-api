package br.com.ares.serviceorder.domain.model;

import br.com.ares.shared.domain.BusinessException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public record ServiceOrder(UUID id, UUID tenantId, UUID customerId, UUID assetId, Set<UUID> serviceIds,
                           String title, String description, ServiceOrderStatus status,
                           ServiceOrderPriority priority, BigDecimal estimatedValue, BigDecimal finalValue,
                           UUID assignedTechnicianId, Instant openedAt, Instant dueAt, Instant completedAt,
                           Instant createdAt, Instant updatedAt) {

    private static final Map<ServiceOrderStatus, Set<ServiceOrderStatus>> TRANSITIONS = Map.of(
            ServiceOrderStatus.OPEN, Set.of(ServiceOrderStatus.IN_DIAGNOSIS, ServiceOrderStatus.CANCELLED),
            ServiceOrderStatus.IN_DIAGNOSIS, Set.of(ServiceOrderStatus.WAITING_APPROVAL,
                    ServiceOrderStatus.IN_PROGRESS, ServiceOrderStatus.CANCELLED),
            ServiceOrderStatus.WAITING_APPROVAL, Set.of(ServiceOrderStatus.IN_PROGRESS, ServiceOrderStatus.CANCELLED),
            ServiceOrderStatus.IN_PROGRESS, Set.of(ServiceOrderStatus.COMPLETED, ServiceOrderStatus.CANCELLED),
            ServiceOrderStatus.COMPLETED, Set.of(),
            ServiceOrderStatus.CANCELLED, Set.of());

    public ServiceOrder changeStatus(ServiceOrderStatus next, BigDecimal newFinalValue, Instant at) {
        if (!TRANSITIONS.get(status).contains(next)) {
            throw BusinessException.badRequest("invalid_order_transition",
                    "Transição inválida de " + status + " para " + next + ".");
        }
        Instant completed = next == ServiceOrderStatus.COMPLETED ? at : completedAt;
        BigDecimal resultingFinalValue = newFinalValue == null ? finalValue : newFinalValue;
        return new ServiceOrder(id, tenantId, customerId, assetId, serviceIds, title, description, next,
                priority, estimatedValue, resultingFinalValue, assignedTechnicianId, openedAt, dueAt,
                completed, createdAt, at);
    }
}
