package br.com.ares.tenant.application.port.in;

import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface SubscriptionUpgradeUseCase {

    UpgradeOptions options();

    UpgradeResult upgrade(UpgradeCommand command);

    record UpgradeOptions(boolean upgradeEnabled, SubscriptionPlan currentPlan, List<PlanOption> plans,
                          BigDecimal additionalUserMonthlyPrice, BigDecimal additionalUserAnnualPrice) {
    }

    record PlanOption(SubscriptionPlan code, String name, BigDecimal monthlyPrice, BigDecimal annualPrice,
                      int includedUsers, List<String> features) {
    }

    record UpgradeCommand(SubscriptionPlan plan, SubscriptionBillingCycle billingCycle,
                          int additionalUserSeats, boolean simulatedPaymentApproved) {
    }

    record UpgradeResult(SubscriptionPlan plan, String planName, SubscriptionBillingCycle billingCycle,
                         int additionalUserSeats, int userLimit, BigDecimal price,
                         BigDecimal monthlyEquivalent, boolean subscriptionActive, Instant paidUntil) {
    }
}
