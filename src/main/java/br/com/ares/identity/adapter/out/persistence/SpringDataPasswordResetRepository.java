package br.com.ares.identity.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataPasswordResetRepository extends JpaRepository<PasswordResetJpaEntity, UUID> {
    Optional<PasswordResetJpaEntity> findByTokenHash(String tokenHash);
}
