package br.com.ares.servicecatalog.adapter.out.persistence;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

interface SpringDataCatalogServiceRepository extends JpaRepository<CatalogServiceJpaEntity,UUID> {
    Optional<CatalogServiceJpaEntity> findByIdAndTenantId(UUID id,UUID tenantId);
    List<CatalogServiceJpaEntity> findAllByTenantIdOrderByNameAsc(UUID tenantId);
    @Query("select count(c) from CatalogServiceJpaEntity c where c.tenantId=:tenantId and c.id in :ids and c.active=true")
    long countActive(@Param("tenantId") UUID tenantId,@Param("ids") Set<UUID> ids);
}
