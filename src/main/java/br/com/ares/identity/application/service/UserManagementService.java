package br.com.ares.identity.application.service;

import br.com.ares.identity.application.port.in.UserManagementUseCase;
import br.com.ares.identity.application.port.out.PasswordHasher;
import br.com.ares.identity.application.port.out.RefreshSessionRepository;
import br.com.ares.identity.application.port.out.UserRepository;
import br.com.ares.identity.domain.model.*;
import br.com.ares.identity.domain.service.PasswordPolicy;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Service
public class UserManagementService implements UserManagementUseCase {

    private final UserRepository users;
    private final RefreshSessionRepository sessions;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public UserManagementService(UserRepository users, RefreshSessionRepository sessions,
                                 PasswordHasher passwordHasher, PasswordPolicy passwordPolicy,
                                 CurrentActorProvider currentActor, AuditLogPort audit, Clock clock) {
        this.users = users;
        this.sessions = sessions;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UserView create(CreateUserCommand command) {
        var actor = currentActor.requiredActor();
        passwordPolicy.requireConfirmation(command.password(), command.passwordConfirmation());
        passwordPolicy.validate(command.password());
        String email = command.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) {
            throw BusinessException.conflict("email_exists", "Este e-mail já está cadastrado.");
        }
        if (command.roles() == null || command.roles().isEmpty()) {
            throw BusinessException.badRequest("role_required", "O usuário deve possuir pelo menos um perfil.");
        }
        if (command.roles().contains(Role.SUPER_ADMIN)) {
            throw BusinessException.forbidden("super_admin_context", "SUPER_ADMIN não pertence a um tenant comum.");
        }
        if (command.roles().contains(Role.CUSTOMER) && command.customerId() == null) {
            throw BusinessException.badRequest("customer_link_required",
                    "Usuários CUSTOMER devem estar vinculados a um cliente.");
        }
        EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
        permissions.addAll(RolePermissions.defaultsFor(command.roles()));
        if (command.extraPermissions() != null) permissions.addAll(command.extraPermissions());
        Instant now = clock.instant();
        var user = new User(UUID.randomUUID(), actor.tenantId(), command.customerId(), command.name().trim(),
                email, passwordHasher.hash(command.password()), command.phone(), command.jobTitle(),
                UserStatus.ACTIVE, Set.copyOf(command.roles()), Set.copyOf(permissions), null, now, now, now);
        user = users.save(user);
        audit.record(actor.tenantId(), actor.userId(), "USER_CREATED", "USER", user.id().toString(),
                Map.of("roles", user.roles()));
        return view(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserView> list() {
        UUID tenantId = currentActor.requiredActor().tenantId();
        return users.findAllByTenantId(tenantId).stream().map(this::view).toList();
    }

    @Override
    @Transactional
    public UserView changeStatus(UUID id, UserStatus status) {
        var actor = currentActor.requiredActor();
        if (actor.userId().equals(id) && status != UserStatus.ACTIVE) {
            throw BusinessException.badRequest("cannot_block_self", "Você não pode bloquear sua própria conta.");
        }
        User user = users.findByIdAndTenantId(id, actor.tenantId()).orElseThrow(() ->
                BusinessException.notFound("user_not_found", "Usuário não encontrado."));
        user = users.save(user.withStatus(status, clock.instant()));
        if (status != UserStatus.ACTIVE) sessions.revokeAllByUserId(user.id(), clock.instant());
        audit.record(actor.tenantId(), actor.userId(), "USER_STATUS_CHANGED", "USER", user.id().toString(),
                Map.of("status", status.name()));
        return view(user);
    }

    private UserView view(User user) {
        return new UserView(user.id(), user.name(), user.email(), user.phone(), user.jobTitle(), user.status(),
                user.roles(), user.permissions(), user.customerId());
    }
}
