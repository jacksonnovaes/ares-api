package br.com.ares.identity.application.port.out;

import br.com.ares.identity.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String normalizedEmail);
    Optional<User> findById(UUID id);
    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);
    List<User> findAllByTenantId(UUID tenantId);
    boolean existsByEmail(String normalizedEmail);
}
