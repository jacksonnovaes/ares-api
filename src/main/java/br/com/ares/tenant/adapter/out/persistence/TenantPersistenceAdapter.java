package br.com.ares.tenant.adapter.out.persistence;

import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.Tenant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class TenantPersistenceAdapter implements TenantRepository {

    private final SpringDataTenantRepository repository;
    private final JdbcTemplate jdbc;

    TenantPersistenceAdapter(SpringDataTenantRepository repository, JdbcTemplate jdbc) {
        this.repository = repository;
        this.jdbc = jdbc;
    }

    @Override
    public Tenant save(Tenant tenant) {
        return toDomain(repository.save(toEntity(tenant)));
    }

    @Override
    public Optional<Tenant> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Tenant> findBySlug(String slug) {
        return repository.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return repository.existsBySlug(slug);
    }

    @Override
    public boolean existsByDocument(String document) {
        return repository.existsByDocument(document.replaceAll("\\D", ""));
    }

    @Override
    public void deleteAllData(UUID tenantId) {
        jdbc.update("DELETE FROM service_order_lines WHERE service_order_id IN "
                + "(SELECT id FROM service_orders WHERE tenant_id = ?)", tenantId);
        jdbc.update("DELETE FROM service_order_services WHERE service_order_id IN "
                + "(SELECT id FROM service_orders WHERE tenant_id = ?)", tenantId);
        jdbc.update("DELETE FROM service_orders WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM refresh_tokens WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM password_reset_tokens WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM user_permissions WHERE user_id IN (SELECT id FROM users WHERE tenant_id = ?)",
                tenantId);
        jdbc.update("DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE tenant_id = ?)",
                tenantId);
        jdbc.update("DELETE FROM users WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM assets WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM customers WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM catalog_services WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM service_order_statuses WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM asset_types WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM audit_events WHERE tenant_id = ?", tenantId);
        jdbc.update("DELETE FROM tenants WHERE id = ?", tenantId);
    }

    private TenantJpaEntity toEntity(Tenant tenant) {
        var entity = new TenantJpaEntity();
        entity.id = tenant.id();
        entity.legalName = tenant.legalName();
        entity.tradeName = tenant.tradeName();
        entity.slug = tenant.slug();
        entity.document = tenant.document();
        entity.status = tenant.status();
        entity.logoUrl = tenant.logoUrl();
        entity.primaryColor = tenant.primaryColor();
        entity.requireAssets = tenant.requireAssets();
        entity.subscriptionPlan = tenant.subscriptionPlan();
        entity.subscriptionActive = tenant.subscriptionActive();
        entity.subscriptionPaidUntil = tenant.subscriptionPaidUntil();
        entity.subscriptionMonthlyPrice = tenant.subscriptionMonthlyPrice();
        entity.couponCode = tenant.couponCode();
        entity.couponDiscountPercentage = tenant.couponDiscountPercentage();
        entity.quoteCalculationMethod = tenant.quoteCalculationMethod();
        entity.defaultSquareMeterPrice = tenant.defaultSquareMeterPrice();
        entity.defaultCubicMeterPrice = tenant.defaultCubicMeterPrice();
        entity.createdAt = tenant.createdAt();
        entity.updatedAt = tenant.updatedAt();
        return entity;
    }

    private Tenant toDomain(TenantJpaEntity entity) {
        return new Tenant(entity.id, entity.legalName, entity.tradeName, entity.slug, entity.document,
                entity.status, entity.logoUrl, entity.primaryColor, entity.requireAssets, entity.subscriptionPlan,
                entity.subscriptionActive, entity.subscriptionPaidUntil, entity.subscriptionMonthlyPrice,
                entity.couponCode, entity.couponDiscountPercentage, entity.quoteCalculationMethod,
                entity.defaultSquareMeterPrice, entity.defaultCubicMeterPrice,
                entity.createdAt, entity.updatedAt);
    }
}
