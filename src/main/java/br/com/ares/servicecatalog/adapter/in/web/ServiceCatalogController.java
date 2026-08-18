package br.com.ares.servicecatalog.adapter.in.web;

import br.com.ares.servicecatalog.application.port.in.ServiceCatalogUseCase;
import br.com.ares.servicecatalog.domain.model.CatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceCatalogController {
    private final ServiceCatalogUseCase services;

    public ServiceCatalogController(ServiceCatalogUseCase services) {
        this.services = services;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SERVICE_CREATE')")
    CatalogService create(@Valid @RequestBody CreateServiceRequest r) {
        return services.create(
                new ServiceCatalogUseCase.CreateServiceCommand(r.name(), r.description(), r.basePrice(), r.estimatedMinutes()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SERVICE_READ')")
    List<CatalogService> list() {
        return services.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SERVICE_READ')")
    CatalogService get(@PathVariable UUID id) {
        return services.get(id);
    }

    record CreateServiceRequest(@NotBlank @Size(max = 160) String name, @Size(max = 2000) String description,
                                @NotNull @DecimalMin("0.00") BigDecimal basePrice,
                                @Positive Integer estimatedMinutes) {
    }
}
