package br.com.ares.customer.application.service;

import br.com.ares.customer.application.port.in.CustomerDirectory;
import br.com.ares.customer.application.port.in.CustomerUseCase;
import br.com.ares.customer.application.port.out.CustomerRepository;
import br.com.ares.customer.domain.model.Customer;
import br.com.ares.customer.domain.model.CustomerStatus;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomerService implements CustomerUseCase, CustomerDirectory {

    private final CustomerRepository repository;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public CustomerService(CustomerRepository repository, CurrentActorProvider currentActor,
                           AuditLogPort audit, Clock clock) {
        this.repository = repository;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Customer create(CreateCustomerCommand command) {
        var actor = currentActor.requiredActor();
        String document = normalizeDocument(command.document());
        if (!document.isBlank() && repository.existsByTenantIdAndDocument(actor.tenantId(), document)) {
            throw BusinessException.conflict("customer_document_exists",
                    "Já existe um cliente com este documento neste tenant.");
        }
        Instant now = clock.instant();
        var customer = new Customer(UUID.randomUUID(), actor.tenantId(), command.type(), command.name().trim(),
                document, normalizeNullable(command.email()), command.phone(), command.notes(),
                CustomerStatus.ACTIVE, now, now);
        customer = repository.save(customer);
        audit.record(actor.tenantId(), actor.userId(), "CUSTOMER_CREATED", "CUSTOMER",
                customer.id().toString(), Map.of());
        return customer;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer get(UUID id) {
        var actor = currentActor.requiredActor();
        if (actor.hasRole("CUSTOMER") && !id.equals(actor.customerId())) {
            throw BusinessException.notFound("customer_not_found", "Cliente não encontrado.");
        }
        return required(id, actor.tenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> list() {
        var actor = currentActor.requiredActor();
        if (actor.hasRole("CUSTOMER")) {
            return actor.customerId() == null ? List.of() : List.of(required(actor.customerId(), actor.tenantId()));
        }
        return repository.findAllByTenantId(actor.tenantId());
    }

    @Override
    @Transactional
    public Customer update(UUID id, UpdateCustomerCommand command) {
        var actor = currentActor.requiredActor();
        Customer current = required(id, actor.tenantId());
        Customer updated = new Customer(current.id(), current.tenantId(), current.type(), command.name().trim(),
                current.document(), normalizeNullable(command.email()), command.phone(), command.notes(),
                current.status(), current.createdAt(), clock.instant());
        updated = repository.save(updated);
        audit.record(actor.tenantId(), actor.userId(), "CUSTOMER_UPDATED", "CUSTOMER",
                updated.id().toString(), Map.of());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID tenantId, UUID customerId) {
        return repository.existsByIdAndTenantId(customerId, tenantId);
    }

    private Customer required(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).orElseThrow(() ->
                BusinessException.notFound("customer_not_found", "Cliente não encontrado."));
    }

    private String normalizeDocument(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
