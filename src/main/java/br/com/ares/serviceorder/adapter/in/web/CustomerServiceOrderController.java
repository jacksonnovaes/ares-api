package br.com.ares.serviceorder.adapter.in.web;

import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusDirectory;
import br.com.ares.serviceorder.domain.model.ServiceOrder;
import br.com.ares.serviceorder.domain.model.ServiceOrderPriority;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
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
    private final ServiceOrderStatusDirectory statuses;

    public CustomerServiceOrderController(ServiceOrderUseCase orders, ServiceOrderStatusDirectory statuses) {
        this.orders = orders;
        this.statuses = statuses;
    }

    @GetMapping
    List<CustomerServiceOrderView> list() {
        return orders.list().stream().map(this::view).toList();
    }

    @GetMapping("/{id}")
    CustomerServiceOrderView get(@PathVariable UUID id) {
        return view(orders.get(id));
    }

    private CustomerServiceOrderView view(ServiceOrder order) {
        String statusName = statuses.required(order.tenantId(), order.status()).name();
        return CustomerServiceOrderView.from(order, statusName);
    }

    record CustomerServiceOrderView(
            UUID id,
            UUID assetId,
            Set<UUID> serviceIds,
            List<CustomerQuoteLineView> quoteLines,
            String title,
            String description,
            String status,
            String statusName,
            ServiceOrderPriority priority,
            BigDecimal estimatedValue,
            BigDecimal finalValue,
            Instant openedAt,
            Instant dueAt,
            Instant completedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        static CustomerServiceOrderView from(ServiceOrder order, String statusName) {
            return new CustomerServiceOrderView(order.id(), order.assetId(), order.serviceIds(),
                    order.quoteLines().stream().map(line -> new CustomerQuoteLineView(line.description(),
                            line.quantity(), line.unit(), line.unitPrice(), line.calculationMethod(),
                            line.widthMeters(), line.lengthMeters(), line.heightMeters(),
                            line.billableQuantity(), line.total())).toList(),
                    order.title(), order.description(), order.status(), statusName, order.priority(),
                    order.estimatedValue(), order.finalValue(), order.openedAt(), order.dueAt(),
                    order.completedAt(), order.createdAt(), order.updatedAt());
        }
    }

    record CustomerQuoteLineView(String description, BigDecimal quantity, String unit,
                                 BigDecimal unitPrice, QuoteCalculationMethod calculationMethod,
                                 BigDecimal widthMeters, BigDecimal lengthMeters, BigDecimal heightMeters,
                                 BigDecimal billableQuantity, BigDecimal total) {
    }
}
