package br.com.ares.serviceorder.adapter.out.persistence;

import br.com.ares.serviceorder.application.port.out.ServiceOrderRepository;
import br.com.ares.serviceorder.domain.model.ServiceOrder;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
class ServiceOrderPersistenceAdapter implements ServiceOrderRepository{
    private final SpringDataServiceOrderRepository repository;
    ServiceOrderPersistenceAdapter(SpringDataServiceOrderRepository repository){this.repository=repository;}
    @Override public ServiceOrder save(ServiceOrder v){return toDomain(repository.save(toEntity(v)));}
    @Override public Optional<ServiceOrder> findByIdAndTenantId(UUID id,UUID tenantId){return repository
            .findByIdAndTenantId(id,tenantId).map(this::toDomain);}
    @Override public List<ServiceOrder> findAllByTenantId(UUID t){return repository
            .findAllByTenantIdOrderByCreatedAtDesc(t).stream().map(this::toDomain).toList();}
    @Override public List<ServiceOrder> findAllByTenantIdAndCustomerId(UUID t,UUID c){return repository
            .findAllByTenantIdAndCustomerIdOrderByCreatedAtDesc(t,c).stream().map(this::toDomain).toList();}
    @Override public List<ServiceOrder> findAllByTenantIdAndAssignedTechnicianId(UUID t,UUID u){return repository
            .findAllByTenantIdAndAssignedTechnicianIdOrderByCreatedAtDesc(t,u).stream().map(this::toDomain).toList();}
    private ServiceOrderJpaEntity toEntity(ServiceOrder v){var e=new ServiceOrderJpaEntity();e.id=v.id();e.tenantId=v.tenantId();
        e.customerId=v.customerId();e.assetId=v.assetId();e.serviceIds=new LinkedHashSet<>(v.serviceIds());e.title=v.title();
        e.description=v.description();e.status=v.status();e.priority=v.priority();e.estimatedValue=v.estimatedValue();
        e.finalValue=v.finalValue();e.assignedTechnicianId=v.assignedTechnicianId();e.openedAt=v.openedAt();e.dueAt=v.dueAt();
        e.completedAt=v.completedAt();e.createdAt=v.createdAt();e.updatedAt=v.updatedAt();return e;}
    private ServiceOrder toDomain(ServiceOrderJpaEntity e){return new ServiceOrder(e.id,e.tenantId,e.customerId,e.assetId,
            Set.copyOf(e.serviceIds),e.title,e.description,e.status,e.priority,e.estimatedValue,e.finalValue,
            e.assignedTechnicianId,e.openedAt,e.dueAt,e.completedAt,e.createdAt,e.updatedAt);}
}
