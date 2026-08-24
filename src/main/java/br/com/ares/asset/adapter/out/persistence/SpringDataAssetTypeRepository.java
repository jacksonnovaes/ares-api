package br.com.ares.asset.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataAssetTypeRepository extends JpaRepository<AssetTypeJpaEntity, UUID> {

    List<AssetTypeJpaEntity> findAllByTenantIdOrderBySystemDefaultDescNameAsc(UUID tenantId);

    Optional<AssetTypeJpaEntity> findByTenantIdAndCode(UUID tenantId, String code);

    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);
}
