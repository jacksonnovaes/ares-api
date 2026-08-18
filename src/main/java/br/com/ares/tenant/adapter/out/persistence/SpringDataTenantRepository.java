package br.com.ares.tenant.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataTenantRepository extends JpaRepository<TenantJpaEntity, UUID> {
    Optional<TenantJpaEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByDocument(String document);
}
