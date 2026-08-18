package br.com.ares.identity.application.service;

import br.com.ares.identity.application.port.in.RegistrationUseCase;
import br.com.ares.identity.application.port.out.PasswordHasher;
import br.com.ares.identity.application.port.out.UserRepository;
import br.com.ares.identity.domain.model.*;
import br.com.ares.identity.domain.service.PasswordPolicy;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class RegistrationService implements RegistrationUseCase {

    private final TenantManagementUseCase tenants;
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final AuditLogPort audit;
    private final Clock clock;

    public RegistrationService(TenantManagementUseCase tenants, UserRepository users,
                               PasswordHasher passwordHasher, PasswordPolicy passwordPolicy,
                               AuditLogPort audit, Clock clock) {
        this.tenants = tenants;
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RegistrationResult register(RegisterTenantAdminCommand command) {
        passwordPolicy.requireConfirmation(command.password(), command.passwordConfirmation());
        passwordPolicy.validate(command.password());
        String email = normalizeEmail(command.adminEmail());
        if (users.existsByEmail(email)) {
            throw BusinessException.conflict("email_exists", "Este e-mail já está cadastrado.");
        }

        var tenant = tenants.create(new TenantManagementUseCase.CreateTenantCommand(
                command.legalName(), command.tradeName(), command.slug(), command.document(),
                command.logoUrl(), command.primaryColor()));
        Instant now = clock.instant();
        Set<Role> roles = Set.of(Role.ADMIN);
        var user = new User(UUID.randomUUID(), tenant.id(), null, command.adminName().trim(), email,
                passwordHasher.hash(command.password()), null, "Administrador", UserStatus.ACTIVE,
                roles, RolePermissions.defaultsFor(roles), null, now, now, now);
        user = users.save(user);
        audit.record(tenant.id(), user.id(), "TENANT_REGISTERED", "TENANT",
                tenant.id().toString(), Map.of("slug", tenant.slug()));
        return new RegistrationResult(tenant.id(), user.id(), tenant.slug());
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
