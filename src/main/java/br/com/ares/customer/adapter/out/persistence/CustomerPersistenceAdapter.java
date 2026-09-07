package br.com.ares.customer.adapter.out.persistence;

import br.com.ares.customer.adapter.out.mapper.CustomerPersistenceMapper;
import br.com.ares.customer.application.port.out.CustomerRepository;
import br.com.ares.customer.domain.model.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static br.com.ares.customer.adapter.out.mapper.CustomerPersistenceMapper.toDomain;
import static br.com.ares.customer.adapter.out.mapper.CustomerPersistenceMapper.toEntity;

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
        return repository.findByIdAndTenantId(id, tenantId).map(CustomerPersistenceMapper::toDomain);
    }

    @Override
    public List<Customer> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantIdOrderByNameAsc(tenantId).stream().map(CustomerPersistenceMapper::toDomain).toList();
    }

    @Override
    public boolean existsByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.existsByIdAndTenantId(id, tenantId);
    }

    @Override
    public boolean existsByTenantIdAndDocument(UUID tenantId, String document) {
        return repository.existsByTenantIdAndDocument(tenantId, document);
    }
}
