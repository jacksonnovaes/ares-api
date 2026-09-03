package br.com.ares.customer.adapter.in.web;

import br.com.ares.customer.application.port.in.CustomerRegistrationUseCase;
import br.com.ares.customer.application.port.in.CustomerUseCase;
import br.com.ares.customer.domain.model.Customer;
import br.com.ares.customer.domain.model.CustomerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    Customer create(@Valid @RequestBody CreateCustomerRequest request) {
        return registration.create(new CustomerRegistrationUseCase.CreateCustomerRegistrationCommand(
                request.type(), request.name(), request.document(), request.email(), request.phone(),
                request.address(), request.notes(), request.createUserAccess(), request.password(),
                request.passwordConfirmation()));
    }

    @GetMapping @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    List<Customer> list() { return customers.list(); }

    @GetMapping("/{id}") @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    Customer get(@PathVariable UUID id) { return customers.get(id); }

    @PutMapping("/{id}") @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    Customer update(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request) {
        return customers.update(id, new CustomerUseCase.UpdateCustomerCommand(
                request.name(), request.email(), request.phone(), request.address(), request.notes()));
    }

    record CreateCustomerRequest(@NotNull CustomerType type, @NotBlank @Size(max = 160) String name,
                                 @Size(max = 20) String document, @Email @Size(max = 254) String email,
                                 @Size(max = 30) String phone, @NotBlank @Size(max = 500) String address,
                                 @Size(max = 2000) String notes,
                                 boolean createUserAccess, @Size(max = 72) String password,
                                 @Size(max = 72) String passwordConfirmation) {}
    record UpdateCustomerRequest(@NotBlank @Size(max = 160) String name,
                                 @Email @Size(max = 254) String email,
                                 @Size(max = 30) String phone, @NotBlank @Size(max = 500) String address,
                                 @Size(max = 2000) String notes) {}
}
