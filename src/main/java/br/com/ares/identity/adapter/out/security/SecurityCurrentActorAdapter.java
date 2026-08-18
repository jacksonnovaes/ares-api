package br.com.ares.identity.adapter.out.security;

import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
class SecurityCurrentActorAdapter implements CurrentActorProvider {

    @Override
    public AuthenticatedActor requiredActor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
            throw BusinessException.unauthorized("authentication_required", "Autenticação obrigatória.");
        }
        return new AuthenticatedActor(principal.userId(), principal.tenantId(), principal.email(),
                principal.roles(), principal.permissions(), principal.customerId());
    }
}
