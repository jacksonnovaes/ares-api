package br.com.ares.tenant.adapter.in.web;

import br.com.ares.tenant.application.port.in.CompanySettingsUseCase;
import br.com.ares.tenant.application.port.in.SubscriptionUpgradeUseCase;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/company-settings")
public class CompanySettingsController {

    private final CompanySettingsUseCase settings;
    private final SubscriptionUpgradeUseCase subscriptionUpgrade;

    public CompanySettingsController(CompanySettingsUseCase settings,
                                     SubscriptionUpgradeUseCase subscriptionUpgrade) {
        this.settings = settings;
        this.subscriptionUpgrade = subscriptionUpgrade;
    }

    @GetMapping
    CompanySettingsUseCase.CompanySettings get() {
        return settings.get();
    }

    @GetMapping("/subscription-upgrade")
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    SubscriptionUpgradeUseCase.UpgradeOptions subscriptionUpgradeOptions() {
        return subscriptionUpgrade.options();
    }

    @PostMapping("/subscription-upgrade")
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    SubscriptionUpgradeUseCase.UpgradeResult upgradeSubscription(
            @Valid @RequestBody UpgradeSubscriptionRequest request) {
        return subscriptionUpgrade.upgrade(new SubscriptionUpgradeUseCase.UpgradeCommand(request.plan(),
                request.billingCycle(), request.additionalUserSeats(), request.simulatedPaymentApproved()));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    CompanySettingsUseCase.CompanySettings update(@Valid @RequestBody UpdateCompanySettingsRequest request) {
        return settings.update(new CompanySettingsUseCase.UpdateCompanySettingsCommand(request.requireAssets(),
                request.quoteCalculationMethod(), request.enabledQuoteCalculationMethods(),
                request.defaultSquareMeterPrice(),
                request.defaultCubicMeterPrice()));
    }

    record UpgradeSubscriptionRequest(@NotNull SubscriptionPlan plan,
                                      @NotNull SubscriptionBillingCycle billingCycle,
                                      @Min(0) @Max(100) int additionalUserSeats,
                                      boolean simulatedPaymentApproved) {
    }

    record UpdateCompanySettingsRequest(@NotNull Boolean requireAssets,
                                        @NotNull QuoteCalculationMethod quoteCalculationMethod,
                                        @NotEmpty Set<QuoteCalculationMethod> enabledQuoteCalculationMethods,
                                        @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
                                        BigDecimal defaultSquareMeterPrice,
                                        @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
                                        BigDecimal defaultCubicMeterPrice) {
    }
}
