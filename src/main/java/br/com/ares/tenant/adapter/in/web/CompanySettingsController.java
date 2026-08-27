package br.com.ares.tenant.adapter.in.web;

import br.com.ares.tenant.application.port.in.CompanySettingsUseCase;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/company-settings")
public class CompanySettingsController {

    private final CompanySettingsUseCase settings;

    public CompanySettingsController(CompanySettingsUseCase settings) {
        this.settings = settings;
    }

    @GetMapping
    CompanySettingsUseCase.CompanySettings get() {
        return settings.get();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    CompanySettingsUseCase.CompanySettings update(@Valid @RequestBody UpdateCompanySettingsRequest request) {
        return settings.update(new CompanySettingsUseCase.UpdateCompanySettingsCommand(request.requireAssets(),
                request.quoteCalculationMethod(), request.defaultSquareMeterPrice(),
                request.defaultCubicMeterPrice()));
    }

    record UpdateCompanySettingsRequest(@NotNull Boolean requireAssets,
                                        @NotNull QuoteCalculationMethod quoteCalculationMethod,
                                        @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
                                        BigDecimal defaultSquareMeterPrice,
                                        @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
                                        BigDecimal defaultCubicMeterPrice) {
    }
}
