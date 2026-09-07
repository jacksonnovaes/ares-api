package br.com.ares.customer.adapter.in.web;

import br.com.ares.customer.adapter.in.web.request.CreateCustomerRequest;
import br.com.ares.customer.adapter.in.web.request.UpdateCustomerRequest;
import br.com.ares.customer.application.port.in.CustomerRegistrationUseCase;
import br.com.ares.customer.application.port.in.CustomerUseCase;
import br.com.ares.customer.application.port.in.command.UpdateCustomerCommand;
import br.com.ares.customer.domain.model.Customer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerUseCase customers;
    private final CustomerRegistrationUseCase registration;

    public CustomerController(CustomerUseCase customers, CustomerRegistrationUseCase registration) {
        this.customers = customers;
        this.registration = registration;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    public Customer create(@Valid @RequestBody CreateCustomerRequest request) {
        return registration.create(new CustomerRegistrationUseCase.CreateCustomerRegistrationCommand(
                request.type(), request.name(), request.document(), request.email(), request.phone(),
                request.address(), request.notes(), request.createUserAccess(), request.password(),
                request.passwordConfirmation()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    public List<Customer> list() {
        return customers.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    public Customer get(@PathVariable UUID id) {
        return customers.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    public Customer update(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request) {
        return customers.update(id, new UpdateCustomerCommand(
                request.name(), request.email(), request.phone(), request.address(), request.notes()));
    }
}
