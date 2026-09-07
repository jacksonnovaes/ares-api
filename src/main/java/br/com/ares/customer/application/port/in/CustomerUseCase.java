package br.com.ares.customer.application.port.in;

import br.com.ares.customer.application.port.in.command.CreateCustomerCommand;
import br.com.ares.customer.application.port.in.command.UpdateCustomerCommand;
import br.com.ares.customer.domain.model.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerUseCase {
    Customer create(CreateCustomerCommand command);
    Customer get(UUID id);
    List<Customer> list();
    Customer update(UUID id, UpdateCustomerCommand command);
}
