package br.com.ares.customer.adapter.in.web.request;

import br.com.ares.customer.domain.model.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(@NotNull CustomerType type, @NotBlank @Size(max = 160) String name,
                                    @Size(max = 20) String document, @Email @Size(max = 254) String email,
                                    @Size(max = 30) String phone, @NotBlank @Size(max = 500) String address,
                                    @Size(max = 2000) String notes,
                                    boolean createUserAccess, @Size(max = 72) String password,
                                    @Size(max = 72) String passwordConfirmation) {
}