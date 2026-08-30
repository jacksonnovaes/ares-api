package br.com.ares.serviceorder.application.service;

import br.com.ares.asset.application.port.in.AssetDirectory;
import br.com.ares.customer.application.port.in.CustomerDirectory;
import br.com.ares.identity.application.port.in.TenantUserDirectory;
import br.com.ares.servicecatalog.application.port.in.ServiceCatalogDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusDirectory;
import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.serviceorder.application.port.out.ServiceOrderRepository;
import br.com.ares.serviceorder.domain.model.*;
import br.com.ares.shared.application.*;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantSettingsDirectory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ServiceOrderService implements ServiceOrderUseCase {
    private final ServiceOrderRepository repository; private final CustomerDirectory customers;
    private final AssetDirectory assets; private final ServiceCatalogDirectory catalog;
    private final TenantSettingsDirectory tenantSettings;
    private final ServiceOrderStatusDirectory statuses;
    private final TenantUserDirectory users; private final CurrentActorProvider currentActor;
    private final AuditLogPort audit; private final Clock clock;
    public ServiceOrderService(ServiceOrderRepository repository,CustomerDirectory customers,AssetDirectory assets,
            ServiceCatalogDirectory catalog,TenantSettingsDirectory tenantSettings,ServiceOrderStatusDirectory statuses,
            TenantUserDirectory users,CurrentActorProvider currentActor,
            AuditLogPort audit,Clock clock){this.repository=repository;this.customers=customers;this.assets=assets;
        this.catalog=catalog;this.tenantSettings=tenantSettings;this.statuses=statuses;this.users=users;this.currentActor=currentActor;
        this.audit=audit;this.clock=clock;}

    @Override @Transactional
    public ServiceOrder create(CreateOrderCommand c){var actor=currentActor.requiredActor();UUID tenant=actor.tenantId();
        rejectCustomerWrite(actor);
        if(!customers.exists(tenant,c.customerId()))throw BusinessException.notFound("customer_not_found","Cliente não encontrado.");
        List<ServiceOrderLine> quoteLines=toQuoteLines(c.quoteLines());
        Set<UUID> serviceIds=quoteLines.stream().map(ServiceOrderLine::serviceId).filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if(!serviceIds.isEmpty()&&!catalog.allExistAndActive(tenant,serviceIds))throw BusinessException.badRequest(
                "invalid_catalog_services","Um ou mais serviços não existem ou estão inativos.");
        validateAsset(tenant,c.customerId(),c.assetId(),tenantSettings.requireAssets(tenant)
                &&catalog.anyRequiresAsset(tenant,serviceIds));
        if(c.assignedTechnicianId()!=null&&!users.activeUserExists(tenant,c.assignedTechnicianId()))
            throw BusinessException.badRequest("invalid_technician","Técnico inválido para este tenant.");
        Instant now=clock.instant();var order=new ServiceOrder(UUID.randomUUID(),tenant,c.customerId(),c.assetId(),
                serviceIds,quoteLines,c.title().trim(),c.description(),"OPEN",c.priority(),
                quoteTotal(quoteLines),null,c.assignedTechnicianId(),now,c.dueAt(),null,now,now);
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
        rejectCustomerWrite(actor);
        ServiceOrder order=required(id,actor.tenantId());enforceScope(order,actor);
        String status=statuses.requiredActive(actor.tenantId(),c.status()).code();
        order=repository.save(order.changeStatus(status,c.finalValue(),clock.instant()));
        audit.record(actor.tenantId(),actor.userId(),"SERVICE_ORDER_STATUS_CHANGED","SERVICE_ORDER",
                order.id().toString(),Map.of("status",order.status()));return order;}

    @Override @Transactional
    public ServiceOrder updateQuote(UUID id,UpdateQuoteCommand c){var actor=currentActor.requiredActor();
        rejectCustomerWrite(actor);ServiceOrder order=required(id,actor.tenantId());enforceScope(order,actor);
        List<ServiceOrderLine> lines=toQuoteLines(c.quoteLines());
        Set<UUID> serviceIds=lines.stream().map(ServiceOrderLine::serviceId).filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if(!serviceIds.isEmpty()&&!catalog.allExistAndActive(actor.tenantId(),serviceIds))throw BusinessException
                .badRequest("invalid_catalog_services","Um ou mais serviços não existem ou estão inativos.");
        validateAsset(actor.tenantId(),order.customerId(),c.assetId(),tenantSettings.requireAssets(actor.tenantId())
                &&catalog.anyRequiresAsset(actor.tenantId(),serviceIds));
        order=repository.save(order.replaceQuote(lines,c.assetId(),clock.instant()));
        audit.record(actor.tenantId(),actor.userId(),"SERVICE_ORDER_QUOTE_UPDATED","SERVICE_ORDER",
                order.id().toString(),Map.of("lineCount",lines.size(),"estimatedValue",order.estimatedValue()));
        return order;}

    private ServiceOrder required(UUID id,UUID tenant){return repository.findByIdAndTenantId(id,tenant).orElseThrow(()->
            BusinessException.notFound("service_order_not_found","Ordem de serviço não encontrada."));}
    private void rejectCustomerWrite(AuthenticatedActor actor){
        if(actor.hasRole("CUSTOMER"))throw BusinessException.forbidden("customer_read_only",
                "Clientes possuem acesso somente para consulta de ordens de serviço.");}
    private void enforceScope(ServiceOrder order,AuthenticatedActor actor){
        if(actor.hasRole("CUSTOMER")&&!order.customerId().equals(actor.customerId()))throw BusinessException.notFound(
                "service_order_not_found","Ordem de serviço não encontrada.");
        if(actor.hasRole("TECHNICIAN")&&!actor.hasRole("ADMIN")&&!actor.hasRole("MANAGER")
                &&!actor.userId().equals(order.assignedTechnicianId()))throw BusinessException.notFound(
                "service_order_not_found","Ordem de serviço não encontrada.");}

    private List<ServiceOrderLine> toQuoteLines(List<QuoteLineCommand> commands){
        if(commands==null||commands.isEmpty())throw BusinessException.badRequest(
                "quote_lines_required","Adicione pelo menos uma linha ao orçamento.");
        if(commands.size()>100)throw BusinessException.badRequest(
                "quote_lines_limit","O orçamento pode conter no máximo 100 linhas.");
        return commands.stream().map(line->new ServiceOrderLine(line.serviceId(),line.description(),line.notes(),
                line.quantity(),line.unit(),line.unitPrice(),line.calculationMethod(),
                line.widthMeters(),line.lengthMeters(),line.heightMeters())).toList();}
    private void validateAsset(UUID tenantId,UUID customerId,UUID assetId,boolean required){
        if(required&&assetId==null)throw BusinessException.badRequest("maintenance_asset_required",
                "Selecione ou cadastre o ativo que receberá a manutenção.");
        if(assetId!=null&&!assets.belongsToCustomer(tenantId,assetId,customerId))throw BusinessException.badRequest(
                "asset_customer_mismatch","O ativo não pertence ao cliente informado.");
    }
    private BigDecimal quoteTotal(List<ServiceOrderLine> lines){return lines.stream().map(ServiceOrderLine::total)
            .reduce(BigDecimal.ZERO,BigDecimal::add);}
}
