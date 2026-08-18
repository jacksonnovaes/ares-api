package br.com.ares.identity.application.port.out;

import br.com.ares.identity.domain.model.RefreshSession;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository {
    RefreshSession save(RefreshSession session);
    Optional<RefreshSession> findByTokenHash(String tokenHash);
    void revokeFamily(UUID familyId, Instant at);
    void revokeAllByUserId(UUID userId, Instant at);
}
