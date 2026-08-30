package br.com.ares.customer.application.port.in;

import br.com.ares.customer.domain.model.Customer;
import br.com.ares.customer.domain.model.CustomerType;

public interface CustomerRegistrationUseCase {

    Customer create(CreateCustomerRegistrationCommand command);

    record CreateCustomerRegistrationCommand(
            CustomerType type,
            String name,
            String document,
            String email,
            String phone,
            String notes,
            boolean createUserAccess,
            String password,
            String passwordConfirmation
    ) {
    }
}
