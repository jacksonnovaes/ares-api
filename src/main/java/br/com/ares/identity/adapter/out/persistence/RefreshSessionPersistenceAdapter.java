package br.com.ares.identity.adapter.out.persistence;

import br.com.ares.identity.application.port.out.RefreshSessionRepository;
import br.com.ares.identity.domain.model.RefreshSession;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
class RefreshSessionPersistenceAdapter implements RefreshSessionRepository {

    private final SpringDataRefreshSessionRepository repository;

    RefreshSessionPersistenceAdapter(SpringDataRefreshSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshSession save(RefreshSession session) {
        var entity = new RefreshSessionJpaEntity();
        entity.id = session.id();
        entity.familyId = session.familyId();
        entity.userId = session.userId();
        entity.tenantId = session.tenantId();
        entity.tokenHash = session.tokenHash();
        entity.expiresAt = session.expiresAt();
        entity.revokedAt = session.revokedAt();
        entity.replacedBy = session.replacedBy();
        entity.createdAt = session.createdAt();
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<RefreshSession> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void revokeFamily(UUID familyId, Instant at) {
        repository.revokeFamily(familyId, at);
    }

    @Override
    public void revokeAllByUserId(UUID userId, Instant at) {
        repository.revokeAllByUserId(userId, at);
    }

    private RefreshSession toDomain(RefreshSessionJpaEntity entity) {
        return new RefreshSession(entity.id, entity.familyId, entity.userId, entity.tenantId,
                entity.tokenHash, entity.expiresAt, entity.revokedAt, entity.replacedBy, entity.createdAt);
    }
}
