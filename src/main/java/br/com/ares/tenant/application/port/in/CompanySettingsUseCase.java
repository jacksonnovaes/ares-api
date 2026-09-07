package br.com.ares.tenant.application.port.in;

import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public interface CompanySettingsUseCase {

    CompanySettings get();

    CompanySettings update(UpdateCompanySettingsCommand command);

    record CompanySettings(boolean requireAssets, SubscriptionPlan subscriptionPlan,
                           SubscriptionBillingCycle subscriptionBillingCycle,
                           boolean subscriptionActive, Instant subscriptionPaidUntil,
                           BigDecimal subscriptionPrice, String couponCode,
                           BigDecimal couponDiscountPercentage, QuoteCalculationMethod quoteCalculationMethod,
                           Set<QuoteCalculationMethod> enabledQuoteCalculationMethods,
                           BigDecimal defaultSquareMeterPrice, BigDecimal defaultCubicMeterPrice,
                           int includedUserLimit, int additionalUserSeats, int userLimit,
                           BigDecimal additionalUserMonthlyPrice) {
    }

    record UpdateCompanySettingsCommand(boolean requireAssets, QuoteCalculationMethod quoteCalculationMethod,
                                        Set<QuoteCalculationMethod> enabledQuoteCalculationMethods,
                                        BigDecimal defaultSquareMeterPrice, BigDecimal defaultCubicMeterPrice) {
    }
}
