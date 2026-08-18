package br.com.ares.identity.adapter.out.persistence;

import br.com.ares.identity.application.port.out.UserRepository;
import br.com.ares.identity.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository repository;

    UserPersistenceAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        return toDomain(repository.save(toEntity(user)));
    }

    @Override
    public Optional<User> findByEmail(String normalizedEmail) {
        return repository.findByEmail(normalizedEmail).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public List<User> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantIdOrderByNameAsc(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByEmail(String normalizedEmail) {
        return repository.existsByEmail(normalizedEmail);
    }

    private UserJpaEntity toEntity(User user) {
        var entity = new UserJpaEntity();
        entity.id = user.id();
        entity.tenantId = user.tenantId();
        entity.customerId = user.customerId();
        entity.name = user.name();
        entity.email = user.email();
        entity.passwordHash = user.passwordHash();
        entity.phone = user.phone();
        entity.jobTitle = user.jobTitle();
        entity.status = user.status();
        entity.roles = new LinkedHashSet<>(user.roles());
        entity.permissions = new LinkedHashSet<>(user.permissions());
        entity.lastLoginAt = user.lastLoginAt();
        entity.passwordChangedAt = user.passwordChangedAt();
        entity.createdAt = user.createdAt();
        entity.updatedAt = user.updatedAt();
        return entity;
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(entity.id, entity.tenantId, entity.customerId, entity.name, entity.email, entity.passwordHash, entity.phone, entity.jobTitle, entity.status, SetCopy.of(entity.roles), SetCopy.of(entity.permissions), entity.lastLoginAt, entity.passwordChangedAt, entity.createdAt, entity.updatedAt);
    }

    private static final class SetCopy {
        private SetCopy() {
        }

        static <T> java.util.Set<T> of(java.util.Set<T> values) {
            return values == null ? java.util.Set.of() : java.util.Set.copyOf(values);
        }
    }
}
