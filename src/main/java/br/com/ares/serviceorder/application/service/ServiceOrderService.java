package br.com.ares.serviceorder.application.service;

import br.com.ares.asset.application.port.in.AssetDirectory;
import br.com.ares.customer.application.port.in.CustomerDirectory;
import br.com.ares.identity.application.port.in.TenantUserDirectory;
import br.com.ares.servicecatalog.application.port.in.ServiceCatalogDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.serviceorder.application.port.out.ServiceOrderRepository;
import br.com.ares.serviceorder.domain.model.*;
import br.com.ares.shared.application.*;
import br.com.ares.shared.domain.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class ServiceOrderService implements ServiceOrderUseCase {
    private final ServiceOrderRepository repository; private final CustomerDirectory customers;
    private final AssetDirectory assets; private final ServiceCatalogDirectory catalog;
    private final TenantUserDirectory users; private final CurrentActorProvider currentActor;
    private final AuditLogPort audit; private final Clock clock;
    public ServiceOrderService(ServiceOrderRepository repository,CustomerDirectory customers,AssetDirectory assets,
            ServiceCatalogDirectory catalog,TenantUserDirectory users,CurrentActorProvider currentActor,
            AuditLogPort audit,Clock clock){this.repository=repository;this.customers=customers;this.assets=assets;
        this.catalog=catalog;this.users=users;this.currentActor=currentActor;this.audit=audit;this.clock=clock;}

    @Override @Transactional
    public ServiceOrder create(CreateOrderCommand c){var actor=currentActor.requiredActor();UUID tenant=actor.tenantId();
        if(!customers.exists(tenant,c.customerId()))throw BusinessException.notFound("customer_not_found","Cliente não encontrado.");
        if(!assets.belongsToCustomer(tenant,c.assetId(),c.customerId()))throw BusinessException.badRequest(
                "asset_customer_mismatch","O ativo não pertence ao cliente informado.");
        if(!catalog.allExistAndActive(tenant,c.serviceIds()))throw BusinessException.badRequest(
                "invalid_catalog_services","Um ou mais serviços não existem ou estão inativos.");
        if(c.assignedTechnicianId()!=null&&!users.activeUserExists(tenant,c.assignedTechnicianId()))
            throw BusinessException.badRequest("invalid_technician","Técnico inválido para este tenant.");
        Instant now=clock.instant();var order=new ServiceOrder(UUID.randomUUID(),tenant,c.customerId(),c.assetId(),
                Set.copyOf(c.serviceIds()),c.title().trim(),c.description(),ServiceOrderStatus.OPEN,c.priority(),
                c.estimatedValue(),null,c.assignedTechnicianId(),now,c.dueAt(),null,now,now);
        order=repository.save(order);audit.record(tenant,actor.userId(),"SERVICE_ORDER_CREATED","SERVICE_ORDER",
                order.id().toString(),Map.of("customerId",c.customerId()));return order;}

    @Override @Transactional(readOnly=true)
    public ServiceOrder get(UUID id){var actor=currentActor.requiredActor();ServiceOrder order=required(id,actor.tenantId());
        enforceScope(order,actor);return order;}

    @Override @Transactional(readOnly=true)
    public List<ServiceOrder> list(){var actor=currentActor.requiredActor();
        if(actor.hasRole("CUSTOMER"))return actor.customerId()==null?List.of():repository
                .findAllByTenantIdAndCustomerId(actor.tenantId(),actor.customerId());
        if(actor.hasRole("TECHNICIAN")&&!actor.hasRole("ADMIN")&&!actor.hasRole("MANAGER"))return repository
                .findAllByTenantIdAndAssignedTechnicianId(actor.tenantId(),actor.userId());
        return repository.findAllByTenantId(actor.tenantId());}

    @Override @Transactional
    public ServiceOrder changeStatus(UUID id,ChangeStatusCommand c){var actor=currentActor.requiredActor();
        ServiceOrder order=required(id,actor.tenantId());enforceScope(order,actor);
        order=repository.save(order.changeStatus(c.status(),c.finalValue(),clock.instant()));
        audit.record(actor.tenantId(),actor.userId(),"SERVICE_ORDER_STATUS_CHANGED","SERVICE_ORDER",
                order.id().toString(),Map.of("status",order.status().name()));return order;}

    private ServiceOrder required(UUID id,UUID tenant){return repository.findByIdAndTenantId(id,tenant).orElseThrow(()->
            BusinessException.notFound("service_order_not_found","Ordem de serviço não encontrada."));}
    private void enforceScope(ServiceOrder order,AuthenticatedActor actor){
        if(actor.hasRole("CUSTOMER")&&!order.customerId().equals(actor.customerId()))throw BusinessException.notFound(
                "service_order_not_found","Ordem de serviço não encontrada.");
        if(actor.hasRole("TECHNICIAN")&&!actor.hasRole("ADMIN")&&!actor.hasRole("MANAGER")
                &&!actor.userId().equals(order.assignedTechnicianId()))throw BusinessException.notFound(
                "service_order_not_found","Ordem de serviço não encontrada.");}
}
