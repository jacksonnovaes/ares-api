package br.com.ares.tenant.adapter.in.web;

import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/branding")
public class BrandingController {

    private final TenantManagementUseCase tenants;

    public BrandingController(TenantManagementUseCase tenants) {
        this.tenants = tenants;
    }

    @GetMapping
    BrandingResponse bySlug(@RequestParam @NotBlank String slug) {
        var tenant = tenants.findBySlug(slug).orElseThrow(() ->
                BusinessException.notFound("branding_not_found", "Identidade visual não encontrada."));
        return new BrandingResponse(tenant.tradeName(), tenant.slug(), tenant.logoUrl(), tenant.primaryColor(),
                tenant.secondaryColor(), tenant.borderRadius());
    }

    record BrandingResponse(String tradeName, String slug, String logoUrl, String primaryColor,
                            String secondaryColor, int borderRadius) {
    }
}
