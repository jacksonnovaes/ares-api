package br.com.ares.servicecatalog.adapter.out.persistence;

import br.com.ares.servicecatalog.application.port.out.ServiceCatalogRepository;
import br.com.ares.servicecatalog.domain.model.CatalogService;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
class ServiceCatalogPersistenceAdapter implements ServiceCatalogRepository {
    private final SpringDataCatalogServiceRepository repository;
    ServiceCatalogPersistenceAdapter(SpringDataCatalogServiceRepository repository){this.repository=repository;}
    @Override public CatalogService save(CatalogService v){return toDomain(repository.save(toEntity(v)));}
    @Override public Optional<CatalogService> findByIdAndTenantId(UUID id,UUID tenantId){
        return repository.findByIdAndTenantId(id,tenantId).map(this::toDomain);}
    @Override public List<CatalogService> findAllByTenantId(UUID tenantId){return repository
            .findAllByTenantIdOrderByNameAsc(tenantId).stream().map(this::toDomain).toList();}
    @Override public long countActiveByTenantIdAndIds(UUID tenantId,Set<UUID> ids){return repository.countActive(tenantId,ids);}
    @Override public boolean existsMaintenanceByTenantIdAndIds(UUID tenantId,Set<UUID> ids){return repository
            .existsActiveMaintenance(tenantId,ids);}
    private CatalogServiceJpaEntity toEntity(CatalogService v){var e=new CatalogServiceJpaEntity();e.id=v.id();
        e.tenantId=v.tenantId();e.name=v.name();e.description=v.description();e.basePrice=v.basePrice();
        e.estimatedMinutes=v.estimatedMinutes();e.type=v.type();e.active=v.active();e.createdAt=v.createdAt();e.updatedAt=v.updatedAt();return e;}
    private CatalogService toDomain(CatalogServiceJpaEntity e){return new CatalogService(e.id,e.tenantId,e.name,
            e.description,e.basePrice,e.estimatedMinutes,e.type,e.active,e.createdAt,e.updatedAt);}
}
