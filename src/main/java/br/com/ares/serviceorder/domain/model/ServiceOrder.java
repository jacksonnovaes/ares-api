package br.com.ares.serviceorder.domain.model;

import br.com.ares.shared.domain.BusinessException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public record ServiceOrder(UUID id, UUID tenantId, UUID customerId, UUID assetId, Set<UUID> serviceIds,
                           List<ServiceOrderLine> quoteLines, String title, String description, String status,
                           ServiceOrderPriority priority, BigDecimal estimatedValue, BigDecimal finalValue,
                           UUID assignedTechnicianId, Instant openedAt, Instant dueAt, Instant completedAt,
                           ServiceOrderDelivery delivery,
                           Instant createdAt, Instant updatedAt) {

    public ServiceOrder changeStatus(String next, BigDecimal newFinalValue, ServiceOrderDelivery newDelivery,
                                     Instant at) {
        if (next == null || next.isBlank()) {
            throw BusinessException.badRequest("service_order_status_required", "Informe o novo status da ordem.");
        }
        boolean completing = "COMPLETED".equals(next);
        boolean reopening = "COMPLETED".equals(status) && !completing;
        Instant completed = completing ? at : reopening ? null : completedAt;
        ServiceOrderDelivery resultingDelivery = completing ? newDelivery : reopening ? null : delivery;
        BigDecimal resultingFinalValue = newFinalValue == null
                ? completing && finalValue == null ? estimatedValue : finalValue
                : newFinalValue;
        return new ServiceOrder(id, tenantId, customerId, assetId, serviceIds, quoteLines, title, description, next,
                priority, estimatedValue, resultingFinalValue, assignedTechnicianId, openedAt, dueAt,
                completed, resultingDelivery, createdAt, at);
    }

    public ServiceOrder replaceQuote(List<ServiceOrderLine> lines, UUID newAssetId, Instant at) {
        if (lines == null || lines.isEmpty()) {
            throw BusinessException.badRequest("quote_lines_required", "Adicione pelo menos uma linha ao orçamento.");
        }
        List<ServiceOrderLine> snapshot = List.copyOf(lines);
        Set<UUID> selectedServices = snapshot.stream()
                .map(ServiceOrderLine::serviceId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        BigDecimal total = snapshot.stream()
                .map(ServiceOrderLine::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ServiceOrder(id, tenantId, customerId, newAssetId, selectedServices, snapshot, title, description,
                status, priority, total, finalValue, assignedTechnicianId, openedAt, dueAt, completedAt,
                delivery, createdAt, at);
    }
}
