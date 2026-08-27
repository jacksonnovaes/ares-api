package br.com.ares.tenant.application.port.in;

import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;

import java.math.BigDecimal;
import java.time.Instant;

public interface CompanySettingsUseCase {

    CompanySettings get();

    CompanySettings update(UpdateCompanySettingsCommand command);

    record CompanySettings(boolean requireAssets, SubscriptionPlan subscriptionPlan,
                           boolean subscriptionActive, Instant subscriptionPaidUntil,
                           BigDecimal subscriptionMonthlyPrice, String couponCode,
                           BigDecimal couponDiscountPercentage, QuoteCalculationMethod quoteCalculationMethod,
                           BigDecimal defaultSquareMeterPrice, BigDecimal defaultCubicMeterPrice) {
    }

    record UpdateCompanySettingsCommand(boolean requireAssets, QuoteCalculationMethod quoteCalculationMethod,
                                        BigDecimal defaultSquareMeterPrice, BigDecimal defaultCubicMeterPrice) {
    }
}
