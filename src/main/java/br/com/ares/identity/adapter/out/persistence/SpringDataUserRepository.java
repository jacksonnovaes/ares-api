package br.com.ares.identity.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    Optional<UserJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    List<UserJpaEntity> findAllByTenantIdOrderByNameAsc(UUID tenantId);
    boolean existsByEmail(String email);
}
