package br.com.ares.serviceorder.adapter.in.web;

import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.serviceorder.domain.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/v1/service-orders")
public class ServiceOrderController{
    private final ServiceOrderUseCase orders;public ServiceOrderController(ServiceOrderUseCase orders){this.orders=orders;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('SERVICE_ORDER_CREATE')")
    ServiceOrder create(@Valid @RequestBody CreateOrderRequest r){return orders.create(new ServiceOrderUseCase.CreateOrderCommand(
            r.customerId(),r.assetId(),r.serviceIds(),r.title(),r.description(),r.priority(),r.estimatedValue(),
            r.assignedTechnicianId(),r.dueAt()));}
    @GetMapping @PreAuthorize("hasAuthority('SERVICE_ORDER_READ')") List<ServiceOrder> list(){return orders.list();}
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('SERVICE_ORDER_READ')")
    ServiceOrder get(@PathVariable UUID id){return orders.get(id);}
    @PatchMapping("/{id}/status") @PreAuthorize("hasAuthority('SERVICE_ORDER_UPDATE')")
    ServiceOrder status(@PathVariable UUID id,@Valid @RequestBody ChangeStatusRequest r){return orders.changeStatus(id,
            new ServiceOrderUseCase.ChangeStatusCommand(r.status(),r.finalValue()));}
    record CreateOrderRequest(@NotNull UUID customerId,@NotNull UUID assetId,@NotEmpty Set<UUID> serviceIds,
            @NotBlank @Size(max=180)String title,@Size(max=4000)String description,
            @NotNull ServiceOrderPriority priority,@DecimalMin("0.00")BigDecimal estimatedValue,
            UUID assignedTechnicianId,Instant dueAt){}
    record ChangeStatusRequest(@NotNull ServiceOrderStatus status,@DecimalMin("0.00")BigDecimal finalValue){}
}
