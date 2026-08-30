package br.com.ares.customer.application.port.in;

import br.com.ares.customer.domain.model.Customer;
import br.com.ares.customer.domain.model.CustomerType;

import java.util.List;
import java.util.UUID;

public interface CustomerUseCase {
    Customer create(CreateCustomerCommand command);
    Customer get(UUID id);
    List<Customer> list();
    Customer update(UUID id, UpdateCustomerCommand command);

    record CreateCustomerCommand(CustomerType type, String name, String document, String email,
                                 String phone, String address, String notes) {
    }

    record UpdateCustomerCommand(String name, String email, String phone, String address, String notes) {
    }
}
