package br.com.ares.serviceorder.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
interface SpringDataServiceOrderRepository extends JpaRepository<ServiceOrderJpaEntity,UUID>{
    Optional<ServiceOrderJpaEntity> findByIdAndTenantId(UUID id,UUID tenantId);
    List<ServiceOrderJpaEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    List<ServiceOrderJpaEntity> findAllByTenantIdAndCustomerIdOrderByCreatedAtDesc(UUID tenantId,UUID customerId);
    List<ServiceOrderJpaEntity> findAllByTenantIdAndAssignedTechnicianIdOrderByCreatedAtDesc(UUID tenantId,UUID userId);
}
