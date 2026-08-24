package br.com.ares.serviceorder.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataServiceOrderStatusRepository extends JpaRepository<ServiceOrderStatusJpaEntity, UUID> {
    Optional<ServiceOrderStatusJpaEntity> findByTenantIdAndCode(UUID tenantId, String code);
    List<ServiceOrderStatusJpaEntity> findAllByTenantIdOrderByDisplayOrderAscNameAsc(UUID tenantId);
    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);
}
