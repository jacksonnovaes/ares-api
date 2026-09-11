package br.com.ares.identity.application.service;

import br.com.ares.identity.application.port.in.TenantUserDirectory;
import br.com.ares.identity.application.port.out.UserRepository;
import br.com.ares.identity.domain.model.Role;
import br.com.ares.identity.domain.model.User;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdentityAccessService implements TenantUserDirectory {

    private final UserRepository users;
    private final TenantManagementUseCase tenants;

    public IdentityAccessService(UserRepository users, TenantManagementUseCase tenants) {
        this.users = users;
        this.tenants = tenants;
    }

    @Transactional(readOnly = true)
    public User validate(UUID userId, UUID tenantId) {
        User user = users.findByIdAndTenantId(userId, tenantId).orElseThrow(() ->
                BusinessException.unauthorized("invalid_token", "Token inválido."));
        if (!user.canAuthenticate() || !tenants.requiredById(tenantId).isActive()) {
            throw BusinessException.unauthorized("account_unavailable", "Conta ou empresa indisponível.");
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean activeUserExists(UUID tenantId, UUID userId) {
        return users.findByIdAndTenantId(userId, tenantId).map(User::canAuthenticate).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean activeTechnicianExists(UUID tenantId, UUID userId) {
        return users.findByIdAndTenantId(userId, tenantId)
                .filter(User::canAuthenticate)
                .map(user -> user.roles().contains(Role.TECHNICIAN))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantUser> activeTechnicians(UUID tenantId) {
        return users.findAllByTenantId(tenantId).stream()
                .filter(User::canAuthenticate)
                .filter(user -> user.roles().contains(Role.TECHNICIAN))
                .map(user -> new TenantUser(user.id(), user.name()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> userName(UUID tenantId, UUID userId) {
        if (userId == null) return Optional.empty();
        return users.findByIdAndTenantId(userId, tenantId).map(User::name);
    }
}
