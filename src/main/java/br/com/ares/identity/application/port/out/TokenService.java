package br.com.ares.identity.application.port.out;

import br.com.ares.identity.domain.model.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public interface TokenService {
    String createAccessToken(User user);
    IssuedRefreshToken createRefreshToken(User user, UUID familyId);
    RefreshClaims decodeRefreshToken(String token);
    String hash(String token);

    record IssuedRefreshToken(String value, UUID id, Instant expiresAt) {
    }

    record RefreshClaims(UUID tokenId, UUID userId, UUID tenantId, Instant expiresAt) {
    }

    record AccessClaims(UUID userId, UUID tenantId, String email, Set<String> roles,
                        Set<String> permissions, Instant issuedAt, Instant expiresAt) {
    }
}
