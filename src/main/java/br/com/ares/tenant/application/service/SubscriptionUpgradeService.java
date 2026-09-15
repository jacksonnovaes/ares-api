package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.SubscriptionUpgradeUseCase;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;

@Service
public class SubscriptionUpgradeService implements SubscriptionUpgradeUseCase {

    private final TenantRepository tenants;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final SubscriptionPricingService pricing;
    private final Clock clock;
    private final boolean paymentSimulationEnabled;

    public SubscriptionUpgradeService(TenantRepository tenants, CurrentActorProvider currentActor,
                                      AuditLogPort audit, SubscriptionPricingService pricing, Clock clock,
                                      @Value("${ares.features.subscription-payment-simulation-enabled:false}")
                                      boolean paymentSimulationEnabled) {
        this.tenants = tenants;
        this.currentActor = currentActor;
        this.audit = audit;
        this.pricing = pricing;
        this.clock = clock;
        this.paymentSimulationEnabled = paymentSimulationEnabled;
    }

    @Override
    @Transactional(readOnly = true)
    public UpgradeOptions options() {
        var actor = currentActor.requiredActor();
        var tenant = tenants.findById(actor.tenantId()).orElseThrow(() ->
                BusinessException.notFound("tenant_not_found", "Empresa não encontrada."));
        var plans = Arrays.stream(SubscriptionPlan.values())
                .filter(plan -> plan.ordinal() > tenant.subscriptionPlan().ordinal())
                .map(plan -> new PlanOption(plan, plan.displayName(), plan.monthlyPrice(), plan.annualPrice(),
                        plan.includedUsers(), plan.features()))
                .toList();
        return new UpgradeOptions(paymentSimulationEnabled, tenant.subscriptionPlan(), plans,
                SubscriptionPricingService.ADDITIONAL_USER_MONTHLY_PRICE,
                SubscriptionPricingService.ADDITIONAL_USER_ANNUAL_PRICE);
    }

    @Override
    @Transactional
    public UpgradeResult upgrade(UpgradeCommand command) {
        var actor = currentActor.requiredActor();
        var tenant = tenants.findById(actor.tenantId()).orElseThrow(() ->
                BusinessException.notFound("tenant_not_found", "Empresa não encontrada."));
        if (command.plan().ordinal() <= tenant.subscriptionPlan().ordinal()) {
            throw BusinessException.badRequest("subscription_upgrade_required",
                    "Selecione um plano superior ao plano atual.");
        }
        if (!paymentSimulationEnabled) {
            throw BusinessException.badRequest("subscription_checkout_unavailable",
                    "O checkout ainda não está disponível. Entre em contato para alterar o plano.");
        }
        if (!command.simulatedPaymentApproved()) {
            throw BusinessException.badRequest("simulated_payment_confirmation_required",
                    "Confirme o pagamento simulado para concluir o upgrade.");
        }

        var quote = pricing.quote(command.plan(), command.billingCycle(), command.additionalUserSeats(), null);
        var now = clock.instant();
        long paidDays = command.billingCycle() == SubscriptionBillingCycle.ANNUAL ? 365 : 30;
        var paidUntil = now.plus(paidDays, ChronoUnit.DAYS);
        var updated = tenants.save(tenant.withSubscription(command.plan(), command.billingCycle(),
                command.additionalUserSeats(), true, paidUntil, quote.finalPrice(), now));

        var details = new LinkedHashMap<String, Object>();
        details.put("previousPlan", tenant.subscriptionPlan().name());
        details.put("plan", updated.subscriptionPlan().name());
        details.put("billingCycle", updated.subscriptionBillingCycle().name());
        details.put("additionalUserSeats", updated.additionalUserSeats());
        details.put("userLimit", updated.subscriptionUserLimit());
        details.put("price", updated.subscriptionPrice());
        details.put("paymentMode", "SIMULATION");
        audit.record(actor.tenantId(), actor.userId(), "TENANT_SUBSCRIPTION_UPGRADED", "TENANT",
                actor.tenantId().toString(), details);

        return new UpgradeResult(updated.subscriptionPlan(), updated.subscriptionPlan().displayName(),
                updated.subscriptionBillingCycle(), updated.additionalUserSeats(), updated.subscriptionUserLimit(),
                updated.subscriptionPrice(), quote.monthlyEquivalent(), updated.subscriptionActive(),
                updated.subscriptionPaidUntil());
    }
}
