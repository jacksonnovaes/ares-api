package br.com.ares.identity.adapter.out.persistence;

import br.com.ares.identity.application.port.out.PasswordResetRepository;
import br.com.ares.identity.domain.model.PasswordReset;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class PasswordResetPersistenceAdapter implements PasswordResetRepository {

    private final SpringDataPasswordResetRepository repository;

    PasswordResetPersistenceAdapter(SpringDataPasswordResetRepository repository) {
        this.repository = repository;
    }

    @Override
    public PasswordReset save(PasswordReset reset) {
        var entity = new PasswordResetJpaEntity();
        entity.id = reset.id();
        entity.userId = reset.userId();
        entity.tenantId = reset.tenantId();
        entity.tokenHash = reset.tokenHash();
        entity.expiresAt = reset.expiresAt();
        entity.usedAt = reset.usedAt();
        entity.createdAt = reset.createdAt();
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<PasswordReset> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    private PasswordReset toDomain(PasswordResetJpaEntity entity) {
        return new PasswordReset(entity.id, entity.userId, entity.tenantId, entity.tokenHash,
                entity.expiresAt, entity.usedAt, entity.createdAt);
    }
}
