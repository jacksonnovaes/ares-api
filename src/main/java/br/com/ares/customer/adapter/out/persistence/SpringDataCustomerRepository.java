package br.com.ares.customer.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, UUID> {
    Optional<CustomerJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    List<CustomerJpaEntity> findAllByTenantIdOrderByNameAsc(UUID tenantId);
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndDocument(UUID tenantId, String document);
}
