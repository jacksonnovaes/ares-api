package br.com.ares.asset.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

interface SpringDataAssetRepository extends JpaRepository<AssetJpaEntity, UUID> {
    Optional<AssetJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    List<AssetJpaEntity> findAllByTenantIdOrderByNameAsc(UUID tenantId);
    List<AssetJpaEntity> findAllByTenantIdAndCustomerIdOrderByNameAsc(UUID tenantId, UUID customerId);
    boolean existsByIdAndTenantIdAndCustomerId(UUID id, UUID tenantId, UUID customerId);
}
