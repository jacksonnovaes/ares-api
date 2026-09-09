package br.com.ares.tenant.adapter.in.web;

import br.com.ares.tenant.application.port.in.SuperAdminTenantUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tenants")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminTenantController {

    private final SuperAdminTenantUseCase tenants;

    public SuperAdminTenantController(SuperAdminTenantUseCase tenants) {
        this.tenants = tenants;
    }

    @GetMapping
    List<SuperAdminTenantUseCase.TenantView> list() {
        return tenants.list();
    }

    @PatchMapping("/{id}/access")
    SuperAdminTenantUseCase.TenantView setAccess(@PathVariable UUID id,
                                                 @Valid @RequestBody ChangeAccessRequest request) {
        return tenants.setAccess(id, request.enabled());
    }

    record ChangeAccessRequest(@NotNull Boolean enabled) {
    }
}
