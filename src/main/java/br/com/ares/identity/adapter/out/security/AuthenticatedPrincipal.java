package br.com.ares.identity.adapter.out.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedPrincipal(UUID userId, UUID tenantId, String email, Set<String> roles,
                                     Set<String> permissions, UUID customerId) {
}
