package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.SuperAdminTenantUseCase;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.Tenant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SuperAdminTenantService implements SuperAdminTenantUseCase {

    private final TenantRepository tenants;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public SuperAdminTenantService(TenantRepository tenants, CurrentActorProvider currentActor,
                                   AuditLogPort audit, Clock clock) {
        this.tenants = tenants;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantView> list() {
        return tenants.findAll().stream()
                .sorted(Comparator.comparing(Tenant::createdAt).reversed())
                .map(this::view)
                .toList();
    }

    @Override
    @Transactional
    public TenantView setAccess(UUID tenantId, boolean enabled) {
        var actor = currentActor.requiredActor();
        if (!enabled && actor.tenantId().equals(tenantId)) {
            throw BusinessException.badRequest("cannot_block_own_tenant",
                    "Você não pode desativar a empresa vinculada à sua própria conta.");
        }
        Tenant current = tenants.findById(tenantId).orElseThrow(() ->
                BusinessException.notFound("tenant_not_found", "Empresa não encontrada."));
        Tenant updated = tenants.save(current.withAdministrativeAccess(enabled, clock.instant()));
        audit.record(updated.id(), actor.userId(), enabled ? "TENANT_ACCESS_ENABLED" : "TENANT_ACCESS_DISABLED",
                "TENANT", updated.id().toString(), Map.of(
                        "enabled", enabled,
                        "previousStatus", current.status().name(),
                        "previousSubscriptionActive", current.subscriptionActive()));
        return view(updated);
    }

    private TenantView view(Tenant tenant) {
        return new TenantView(tenant.id(), tenant.legalName(), tenant.tradeName(), tenant.slug(), tenant.document(),
                tenant.status(), tenant.subscriptionActive(), tenant.isActive(), tenant.subscriptionPlan(),
                tenant.subscriptionBillingCycle(), tenant.subscriptionPaidUntil(), tenant.createdAt(),
                tenant.updatedAt());
    }
}
