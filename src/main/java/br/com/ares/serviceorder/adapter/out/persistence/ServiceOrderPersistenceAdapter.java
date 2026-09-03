package br.com.ares.serviceorder.adapter.out.persistence;

import br.com.ares.serviceorder.application.port.out.ServiceOrderRepository;
import br.com.ares.serviceorder.domain.model.ServiceOrderDelivery;
import br.com.ares.serviceorder.domain.model.ServiceOrder;
import br.com.ares.serviceorder.domain.model.ServiceOrderLine;
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
        e.customerId=v.customerId();e.assetId=v.assetId();e.serviceIds=new LinkedHashSet<>(v.serviceIds());
        e.quoteLines=v.quoteLines().stream().map(this::toLineEntity).collect(java.util.stream.Collectors.toCollection(ArrayList::new));e.title=v.title();
        e.description=v.description();e.status=v.status();e.priority=v.priority();e.estimatedValue=v.estimatedValue();
        e.finalValue=v.finalValue();e.assignedTechnicianId=v.assignedTechnicianId();e.openedAt=v.openedAt();e.dueAt=v.dueAt();
        e.completedAt=v.completedAt();
        if(v.delivery()!=null){e.deliveredAt=v.delivery().deliveredAt();e.deliveryReceivedBy=v.delivery().receivedBy();
            e.warrantyDays=v.delivery().warrantyDays();e.warrantyUntil=v.delivery().warrantyUntil();
            e.warrantyTerms=v.delivery().warrantyTerms();e.deliveryNotes=v.delivery().notes();}
        e.createdAt=v.createdAt();e.updatedAt=v.updatedAt();return e;}
    private ServiceOrder toDomain(ServiceOrderJpaEntity e){return new ServiceOrder(e.id,e.tenantId,e.customerId,e.assetId,
            Set.copyOf(e.serviceIds),e.quoteLines.stream().map(this::toLineDomain).toList(),e.title,e.description,e.status,e.priority,e.estimatedValue,e.finalValue,
            e.assignedTechnicianId,e.openedAt,e.dueAt,e.completedAt,toDelivery(e),e.createdAt,e.updatedAt);}
    private ServiceOrderDelivery toDelivery(ServiceOrderJpaEntity e){
        if(e.deliveredAt==null)return null;
        return new ServiceOrderDelivery(e.deliveredAt,e.deliveryReceivedBy,e.warrantyDays==null?0:e.warrantyDays,
                e.warrantyUntil,e.warrantyTerms,e.deliveryNotes);}
    private ServiceOrderLineJpaEmbeddable toLineEntity(ServiceOrderLine v){var e=new ServiceOrderLineJpaEmbeddable();
        e.serviceId=v.serviceId();e.description=v.description();e.notes=v.notes();e.quantity=v.quantity();e.unit=v.unit();
        e.unitPrice=v.unitPrice();e.calculationMethod=v.calculationMethod();e.widthMeters=v.widthMeters();
        e.lengthMeters=v.lengthMeters();e.heightMeters=v.heightMeters();return e;}
    private ServiceOrderLine toLineDomain(ServiceOrderLineJpaEmbeddable e){return new ServiceOrderLine(e.serviceId,
            e.description,e.notes,e.quantity,e.unit,e.unitPrice,e.calculationMethod,e.widthMeters,e.lengthMeters,
            e.heightMeters);}
}
