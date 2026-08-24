package br.com.ares.serviceorder.adapter.in.web;

import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusUseCase;
import br.com.ares.serviceorder.domain.model.ServiceOrderStatusDefinition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-order-statuses")
public class ServiceOrderStatusController {

    private final ServiceOrderStatusUseCase statuses;

    public ServiceOrderStatusController(ServiceOrderStatusUseCase statuses) {
        this.statuses = statuses;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SERVICE_ORDER_READ') or hasAuthority('TENANT_CONFIGURE')")
    List<ServiceOrderStatusDefinition> list() {
        return statuses.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    ServiceOrderStatusDefinition create(@Valid @RequestBody CreateStatusRequest request) {
        return statuses.create(new ServiceOrderStatusUseCase.CreateStatusCommand(request.name()));
    }

    record CreateStatusRequest(@NotBlank @Size(max = 100) String name) {
    }
}
