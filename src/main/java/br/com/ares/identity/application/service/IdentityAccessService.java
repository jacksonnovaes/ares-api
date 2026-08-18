package br.com.ares.identity.application.service;

import br.com.ares.identity.application.port.in.TenantUserDirectory;
import br.com.ares.identity.application.port.out.UserRepository;
import br.com.ares.identity.domain.model.User;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
