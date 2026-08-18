package br.com.ares.servicecatalog.application.service;

import br.com.ares.servicecatalog.application.port.in.*;
import br.com.ares.servicecatalog.application.port.out.ServiceCatalogRepository;
import br.com.ares.servicecatalog.domain.model.CatalogService;
import br.com.ares.shared.application.*;
import br.com.ares.shared.domain.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class ServiceCatalogService implements ServiceCatalogUseCase, ServiceCatalogDirectory {
    private final ServiceCatalogRepository repository; private final CurrentActorProvider currentActor;
    private final AuditLogPort audit; private final Clock clock;
    public ServiceCatalogService(ServiceCatalogRepository repository, CurrentActorProvider currentActor,
                                 AuditLogPort audit, Clock clock) {
        this.repository=repository; this.currentActor=currentActor; this.audit=audit; this.clock=clock;
    }
    @Override @Transactional
    public CatalogService create(CreateServiceCommand c) {
        var actor=currentActor.requiredActor(); Instant now=clock.instant();
        var value=new CatalogService(UUID.randomUUID(),actor.tenantId(),c.name().trim(),c.description(),
                c.basePrice(),c.estimatedMinutes(),true,now,now);
        value=repository.save(value);
        audit.record(actor.tenantId(),actor.userId(),"CATALOG_SERVICE_CREATED","CATALOG_SERVICE",
                value.id().toString(),Map.of()); return value;
    }
    @Override @Transactional(readOnly=true)
    public CatalogService get(UUID id) {
        UUID tenantId=currentActor.requiredActor().tenantId();
        return repository.findByIdAndTenantId(id,tenantId).orElseThrow(() ->
                BusinessException.notFound("catalog_service_not_found","Serviço não encontrado."));
    }
    @Override @Transactional(readOnly=true)
    public List<CatalogService> list() { return repository.findAllByTenantId(currentActor.requiredActor().tenantId()); }
    @Override @Transactional(readOnly=true)
    public boolean allExistAndActive(UUID tenantId, Set<UUID> ids) {
        return ids!=null && !ids.isEmpty() && repository.countActiveByTenantIdAndIds(tenantId,ids)==ids.size();
    }
}
