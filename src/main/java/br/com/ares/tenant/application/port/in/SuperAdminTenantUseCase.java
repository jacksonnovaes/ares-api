package br.com.ares.tenant.application.port.in;

import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import br.com.ares.tenant.domain.model.TenantStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SuperAdminTenantUseCase {

    List<TenantView> list();

    TenantView setAccess(UUID tenantId, boolean enabled);

    record TenantView(UUID id, String legalName, String tradeName, String slug, String document,
                      TenantStatus status, boolean subscriptionActive, boolean accessEnabled,
                      SubscriptionPlan subscriptionPlan, SubscriptionBillingCycle subscriptionBillingCycle,
                      Instant subscriptionPaidUntil, Instant createdAt, Instant updatedAt) {
    }
}
