package br.com.ares.serviceorder.adapter.in.web;

import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase;
import br.com.ares.serviceorder.domain.model.*;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/v1/service-orders")
public class ServiceOrderController{
    private final ServiceOrderUseCase orders;
    private final ServiceOrderDocumentUseCase documents;
    public ServiceOrderController(ServiceOrderUseCase orders, ServiceOrderDocumentUseCase documents){
        this.orders=orders;this.documents=documents;
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SERVICE_ORDER_CREATE') and !hasRole('CUSTOMER')")
    ServiceOrder create(@Valid @RequestBody CreateOrderRequest r){return orders.create(new ServiceOrderUseCase.CreateOrderCommand(
            r.customerId(),r.assetId(),toCommands(r.lines()),r.title(),r.description(),r.priority(),
            r.assignedTechnicianId(),r.dueAt()));}
    @GetMapping @PreAuthorize("hasAuthority('SERVICE_ORDER_READ')") List<ServiceOrder> list(){return orders.list();}
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('SERVICE_ORDER_READ')")
    ServiceOrder get(@PathVariable UUID id){return orders.get(id);}
    @GetMapping("/{id}/document") @PreAuthorize("hasAuthority('SERVICE_ORDER_READ')")
    ServiceOrderDocumentUseCase.ServiceOrderDocument document(@PathVariable UUID id){
        return documents.getDocument(id);
    }
    @PostMapping("/{id}/email")
    @PreAuthorize("hasAuthority('SERVICE_ORDER_UPDATE') and !hasRole('CUSTOMER')")
    ServiceOrderDocumentUseCase.EmailDeliveryResult email(@PathVariable UUID id,
            @Valid @RequestBody SendEmailRequest r){
        return documents.sendEmail(id,new ServiceOrderDocumentUseCase.SendEmailCommand(r.recipient()));
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SERVICE_ORDER_UPDATE') and !hasRole('CUSTOMER')")
    ServiceOrder status(@PathVariable UUID id,@Valid @RequestBody ChangeStatusRequest r){return orders.changeStatus(id,
            new ServiceOrderUseCase.ChangeStatusCommand(r.status(),r.finalValue(),r.deliveryReceivedBy(),
                    r.warrantyDays(),r.warrantyTerms(),r.deliveryNotes()));}
    @PutMapping("/{id}/quote")
    @PreAuthorize("hasAuthority('SERVICE_ORDER_UPDATE') and !hasRole('CUSTOMER')")
    ServiceOrder quote(@PathVariable UUID id,@Valid @RequestBody UpdateQuoteRequest r){return orders.updateQuote(id,
            new ServiceOrderUseCase.UpdateQuoteCommand(r.assetId(),toCommands(r.lines())));}
    private List<ServiceOrderUseCase.QuoteLineCommand> toCommands(List<QuoteLineRequest> lines){return lines.stream()
            .map(line->new ServiceOrderUseCase.QuoteLineCommand(line.serviceId(),line.description(),line.notes(),
                    line.quantity(),
                    line.unit(),line.unitPrice(),line.calculationMethod(),line.widthMeters(),
                    line.lengthMeters(),line.heightMeters())).toList();}
    record CreateOrderRequest(@NotNull UUID customerId,UUID assetId,
            @NotEmpty @Size(max=100) List<@Valid QuoteLineRequest> lines,
            @NotBlank @Size(max=180)String title,@Size(max=4000)String description,
            @NotNull ServiceOrderPriority priority,
            UUID assignedTechnicianId,Instant dueAt){}
    record UpdateQuoteRequest(UUID assetId,@NotEmpty @Size(max=100) List<@Valid QuoteLineRequest> lines){}
    record QuoteLineRequest(UUID serviceId,@NotBlank @Size(max=500)String description,
            @Size(max=1000)String notes,
            @NotNull @DecimalMin("0.001") @Digits(integer=9,fraction=3)BigDecimal quantity,
            @NotBlank @Size(max=20)String unit,
            @NotNull @DecimalMin("0.00") @Digits(integer=13,fraction=2)BigDecimal unitPrice,
            QuoteCalculationMethod calculationMethod,
            @DecimalMin("0.001") @Digits(integer=9,fraction=3)BigDecimal widthMeters,
            @DecimalMin("0.001") @Digits(integer=9,fraction=3)BigDecimal lengthMeters,
            @DecimalMin("0.001") @Digits(integer=9,fraction=3)BigDecimal heightMeters){}
    record ChangeStatusRequest(@NotBlank @Size(max=50)String status,
            @DecimalMin("0.00") @Digits(integer=13,fraction=2) BigDecimal finalValue,
            @Size(max=160) String deliveryReceivedBy,
            @Min(0) @Max(3650) Integer warrantyDays,
            @Size(max=2000) String warrantyTerms,
            @Size(max=2000) String deliveryNotes){}
    record SendEmailRequest(@Email @Size(max=254) String recipient){}
}
