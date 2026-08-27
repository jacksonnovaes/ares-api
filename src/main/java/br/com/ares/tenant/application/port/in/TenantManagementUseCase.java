package br.com.ares.tenant.application.port.in;

import br.com.ares.tenant.domain.model.Tenant;
import br.com.ares.tenant.domain.model.TenantStatus;
import br.com.ares.tenant.domain.model.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TenantManagementUseCase {

    Tenant create(CreateTenantCommand command);

    Tenant requiredById(UUID id);

    Optional<Tenant> findBySlug(String slug);

    Tenant changeStatus(UUID id, TenantStatus status);

    record CreateTenantCommand(String legalName, String tradeName, String slug, String document,
                               String logoUrl, String primaryColor, SubscriptionPlan subscriptionPlan,
                               boolean subscriptionActive, Instant subscriptionPaidUntil,
                               BigDecimal subscriptionMonthlyPrice, String couponCode,
                               BigDecimal couponDiscountPercentage) {
    }
}
