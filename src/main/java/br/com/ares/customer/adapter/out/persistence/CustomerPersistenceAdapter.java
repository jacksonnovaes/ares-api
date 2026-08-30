package br.com.ares.customer.adapter.out.persistence;

import br.com.ares.customer.application.port.out.CustomerRepository;
import br.com.ares.customer.domain.model.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class CustomerPersistenceAdapter implements CustomerRepository {
    private final SpringDataCustomerRepository repository;

    CustomerPersistenceAdapter(SpringDataCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer save(Customer customer) {
        return toDomain(repository.save(toEntity(customer)));
    }

    @Override
    public Optional<Customer> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public List<Customer> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantIdOrderByNameAsc(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.existsByIdAndTenantId(id, tenantId);
    }

    @Override
    public boolean existsByTenantIdAndDocument(UUID tenantId, String document) {
        return repository.existsByTenantIdAndDocument(tenantId, document);
    }

    private CustomerJpaEntity toEntity(Customer value) {
        var entity = new CustomerJpaEntity();
        entity.id = value.id();
        entity.tenantId = value.tenantId();
        entity.type = value.type();
        entity.name = value.name();
        entity.document = value.document();
        entity.email = value.email();
        entity.phone = value.phone();
        entity.address = value.address();
        entity.notes = value.notes();
        entity.status = value.status();
        entity.createdAt = value.createdAt();
        entity.updatedAt = value.updatedAt();
        return entity;
    }

    private Customer toDomain(CustomerJpaEntity value) {
        return new Customer(value.id, value.tenantId, value.type, value.name, value.document, value.email,
                value.phone, value.address, value.notes, value.status, value.createdAt, value.updatedAt);
    }
}
