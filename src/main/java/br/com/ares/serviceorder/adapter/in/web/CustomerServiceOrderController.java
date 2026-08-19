package br.com.ares.serviceorder.adapter.in.web;

import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.serviceorder.domain.model.ServiceOrder;
import br.com.ares.serviceorder.domain.model.ServiceOrderPriority;
import br.com.ares.serviceorder.domain.model.ServiceOrderStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer/service-orders")
@PreAuthorize("hasRole('CUSTOMER') and hasAuthority('SERVICE_ORDER_READ')")
public class CustomerServiceOrderController {

    private final ServiceOrderUseCase orders;

    public CustomerServiceOrderController(ServiceOrderUseCase orders) {
        this.orders = orders;
    }

    @GetMapping
    List<CustomerServiceOrderView> list() {
        return orders.list().stream().map(CustomerServiceOrderView::from).toList();
    }

    @GetMapping("/{id}")
    CustomerServiceOrderView get(@PathVariable UUID id) {
        return CustomerServiceOrderView.from(orders.get(id));
    }

    record CustomerServiceOrderView(
            UUID id,
            UUID assetId,
            Set<UUID> serviceIds,
            String title,
            String description,
            ServiceOrderStatus status,
            ServiceOrderPriority priority,
            BigDecimal estimatedValue,
            BigDecimal finalValue,
            Instant openedAt,
            Instant dueAt,
            Instant completedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        static CustomerServiceOrderView from(ServiceOrder order) {
            return new CustomerServiceOrderView(order.id(), order.assetId(), order.serviceIds(),
                    order.title(), order.description(), order.status(), order.priority(),
                    order.estimatedValue(), order.finalValue(), order.openedAt(), order.dueAt(),
                    order.completedAt(), order.createdAt(), order.updatedAt());
        }
    }
}
