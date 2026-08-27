package br.com.ares.tenant.application.service;

import br.com.ares.asset.application.port.in.AssetTypeDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusDirectory;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.CompanySettingsUseCase;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import br.com.ares.tenant.application.port.in.TenantSettingsDirectory;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import br.com.ares.tenant.domain.model.Tenant;
import br.com.ares.tenant.domain.model.TenantStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService implements TenantManagementUseCase, CompanySettingsUseCase, TenantSettingsDirectory {

    private final TenantRepository repository;
    private final AssetTypeDirectory assetTypes;
    private final ServiceOrderStatusDirectory orderStatuses;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public TenantService(TenantRepository repository, AssetTypeDirectory assetTypes,
                         ServiceOrderStatusDirectory orderStatuses, CurrentActorProvider currentActor,
                         AuditLogPort audit, Clock clock) {
        this.repository = repository;
        this.assetTypes = assetTypes;
        this.orderStatuses = orderStatuses;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Tenant create(CreateTenantCommand command) {
        String slug = normalizeSlug(command.slug());
        if (repository.existsBySlug(slug)) {
            throw BusinessException.conflict("tenant_slug_exists", "Este slug já está em uso.");
        }
        if (repository.existsByDocument(command.document())) {
            throw BusinessException.conflict("tenant_document_exists", "Já existe uma empresa com este documento.");
        }
        Instant now = clock.instant();
        var tenant = new Tenant(UUID.randomUUID(), command.legalName().trim(), command.tradeName().trim(),
                slug, onlyDigits(command.document()), TenantStatus.ACTIVE, command.logoUrl(),
                command.primaryColor(), true, command.subscriptionPlan(), command.subscriptionActive(),
                command.subscriptionPaidUntil(), command.subscriptionMonthlyPrice(), command.couponCode(),
                command.couponDiscountPercentage(), QuoteCalculationMethod.QUANTITY, null, null, now, now);
        tenant = repository.save(tenant);
        assetTypes.provisionDefaults(tenant.id());
        orderStatuses.provisionDefaults(tenant.id());
        return tenant;
    }

    @Override
    @Transactional(readOnly = true)
    public Tenant requiredById(UUID id) {
        return repository.findById(id).orElseThrow(() ->
                BusinessException.notFound("tenant_not_found", "Tenant não encontrado."));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findBySlug(String slug) {
        return repository.findBySlug(normalizeSlug(slug));
    }

    @Override
    @Transactional
    public Tenant changeStatus(UUID id, TenantStatus status) {
        Tenant current = requiredById(id);
        return repository.save(new Tenant(current.id(), current.legalName(), current.tradeName(), current.slug(),
                current.document(), status, current.logoUrl(), current.primaryColor(),
                current.requireAssets(), current.subscriptionPlan(), current.subscriptionActive(),
                current.subscriptionPaidUntil(), current.subscriptionMonthlyPrice(), current.couponCode(),
                current.couponDiscountPercentage(), current.quoteCalculationMethod(),
                current.defaultSquareMeterPrice(), current.defaultCubicMeterPrice(), current.createdAt(),
                clock.instant()));
    }

    @Override
    @Transactional(readOnly = true)
    public CompanySettings get() {
        var actor = currentActor.requiredActor();
        return toSettings(requiredById(actor.tenantId()));
    }

    @Override
    @Transactional
    public CompanySettings update(UpdateCompanySettingsCommand command) {
        var actor = currentActor.requiredActor();
        Tenant tenant = requiredById(actor.tenantId());
        BigDecimal squareMeterPrice = command.defaultSquareMeterPrice() == null ? null
                : command.defaultSquareMeterPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal cubicMeterPrice = command.defaultCubicMeterPrice() == null ? null
                : command.defaultCubicMeterPrice().setScale(2, RoundingMode.HALF_UP);
        if (command.quoteCalculationMethod() == QuoteCalculationMethod.SQUARE_METER
                && (squareMeterPrice == null || squareMeterPrice.signum() <= 0)) {
            throw BusinessException.badRequest("square_meter_price_required",
                    "Informe um valor por metro quadrado maior que zero.");
        }
        if (command.quoteCalculationMethod() == QuoteCalculationMethod.CUBIC_METER
                && (cubicMeterPrice == null || cubicMeterPrice.signum() <= 0)) {
            throw BusinessException.badRequest("cubic_meter_price_required",
                    "Informe um valor por metro cúbico maior que zero.");
        }
        Tenant updated = repository.save(tenant.withCompanySettings(command.requireAssets(),
                command.quoteCalculationMethod(), squareMeterPrice, cubicMeterPrice, clock.instant()));
        var details = new LinkedHashMap<String, Object>();
        details.put("requireAssets", updated.requireAssets());
        details.put("quoteCalculationMethod", updated.quoteCalculationMethod().name());
        if (updated.defaultSquareMeterPrice() != null) {
            details.put("defaultSquareMeterPrice", updated.defaultSquareMeterPrice());
        }
        if (updated.defaultCubicMeterPrice() != null) {
            details.put("defaultCubicMeterPrice", updated.defaultCubicMeterPrice());
        }
        audit.record(actor.tenantId(), actor.userId(), "COMPANY_SETTINGS_UPDATED", "TENANT",
                actor.tenantId().toString(), details);
        return toSettings(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean requireAssets(UUID tenantId) {
        return requiredById(tenantId).requireAssets();
    }

    private CompanySettings toSettings(Tenant tenant) {
        return new CompanySettings(tenant.requireAssets(), tenant.subscriptionPlan(), tenant.subscriptionActive(),
                tenant.subscriptionPaidUntil(), tenant.subscriptionMonthlyPrice(), tenant.couponCode(),
                tenant.couponDiscountPercentage(), tenant.quoteCalculationMethod(),
                tenant.defaultSquareMeterPrice(), tenant.defaultCubicMeterPrice());
    }

    private String normalizeSlug(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
