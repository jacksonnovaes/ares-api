package br.com.ares.shared.application;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedActor(
        UUID userId,
        UUID tenantId,
        String email,
        Set<String> roles,
        Set<String> permissions,
        UUID customerId
) {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
