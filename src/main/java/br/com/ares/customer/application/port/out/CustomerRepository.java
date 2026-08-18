package br.com.ares.customer.application.port.out;

import br.com.ares.customer.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Customer> findAllByTenantId(UUID tenantId);
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndDocument(UUID tenantId, String document);
}
