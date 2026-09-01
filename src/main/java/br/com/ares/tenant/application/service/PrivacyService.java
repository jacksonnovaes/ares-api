package br.com.ares.tenant.application.service;

import br.com.ares.asset.application.port.in.AssetUseCase;
import br.com.ares.customer.application.port.in.CustomerUseCase;
import br.com.ares.identity.application.port.out.PasswordHasher;
import br.com.ares.identity.application.port.out.UserRepository;
import br.com.ares.servicecatalog.application.port.in.ServiceCatalogUseCase;
import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.PrivacyUseCase;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import br.com.ares.tenant.application.port.in.PublicProfileMediaUseCase;
import br.com.ares.tenant.application.port.out.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;

@Service
public class PrivacyService implements PrivacyUseCase {

    private final TenantManagementUseCase tenants;
    private final TenantRepository tenantRepository;
    private final UserRepository users;
    private final CustomerUseCase customers;
    private final AssetUseCase assets;
    private final ServiceCatalogUseCase catalog;
    private final ServiceOrderUseCase orders;
    private final PasswordHasher passwordHasher;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;
    private final PublicProfileMediaUseCase publicProfileMedia;

    public PrivacyService(TenantManagementUseCase tenants, TenantRepository tenantRepository,
                          UserRepository users, CustomerUseCase customers, AssetUseCase assets,
                          ServiceCatalogUseCase catalog, ServiceOrderUseCase orders,
                          PasswordHasher passwordHasher, CurrentActorProvider currentActor,
                          AuditLogPort audit, Clock clock, PublicProfileMediaUseCase publicProfileMedia) {
        this.tenants = tenants;
        this.tenantRepository = tenantRepository;
        this.users = users;
        this.customers = customers;
        this.assets = assets;
        this.catalog = catalog;
        this.orders = orders;
        this.passwordHasher = passwordHasher;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
        this.publicProfileMedia = publicProfileMedia;
    }

    @Override
    @Transactional
    public TenantDataExport exportData() {
        var actor = requireTenantAdmin();
        audit.record(actor.tenantId(), actor.userId(), "TENANT_DATA_EXPORTED", "TENANT",
                actor.tenantId().toString(), Map.of("formatVersion", "1.0"));
        var company = tenants.requiredById(actor.tenantId());
        var userData = users.findAllByTenantId(actor.tenantId()).stream()
                .map(user -> new UserData(user.id(), user.customerId(), user.name(), user.email(), user.phone(),
                        user.jobTitle(), user.status(), user.roles(), user.permissions(), user.lastLoginAt(),
                        user.passwordChangedAt(), user.createdAt(), user.updatedAt()))
                .toList();
        return new TenantDataExport("1.0", clock.instant(), company, userData, customers.list(), assets.list(null),
                catalog.list(), orders.list(), audit.findAllByTenantId(actor.tenantId()));
    }

    @Override
    @Transactional
    public DataDeletionResult deleteAccount(DeleteAccountCommand command) {
        var actor = requireTenantAdmin();
        var user = users.findByIdAndTenantId(actor.userId(), actor.tenantId()).orElseThrow(() ->
                BusinessException.unauthorized("user_not_found", "Usuário autenticado não encontrado."));
        if (!passwordHasher.matches(command.currentPassword(), user.passwordHash())) {
            throw BusinessException.badRequest("invalid_current_password", "A senha atual está incorreta.");
        }
        var tenant = tenants.requiredById(actor.tenantId());
        String expectedConfirmation = "EXCLUIR " + tenant.slug();
        if (!expectedConfirmation.equals(command.confirmation())) {
            throw BusinessException.badRequest("invalid_deletion_confirmation",
                    "Digite exatamente " + expectedConfirmation + " para confirmar.");
        }

        UUID receiptId = UUID.randomUUID();
        var deletedAt = clock.instant();
        audit.record(actor.tenantId(), actor.userId(), "TENANT_DELETION_CONFIRMED", "TENANT",
                actor.tenantId().toString(), Map.of("receiptId", receiptId, "deletedAt", deletedAt));
        tenantRepository.deleteAllData(actor.tenantId());
        publicProfileMedia.deleteTenantFiles(actor.tenantId());
        return new DataDeletionResult(receiptId, deletedAt);
    }

    private br.com.ares.shared.application.AuthenticatedActor requireTenantAdmin() {
        var actor = currentActor.requiredActor();
        if (!actor.hasRole("ADMIN")) {
            throw BusinessException.forbidden("tenant_admin_required",
                    "Somente um administrador da empresa pode executar esta operação.");
        }
        return actor;
    }
}
