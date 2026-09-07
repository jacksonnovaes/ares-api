package br.com.ares.customer.adapter.in.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotBlank @Size(max = 160) String name,
        @Email @Size(max = 254) String email,
        @Size(max = 30) String phone, @NotBlank @Size(max = 500) String address,
        @Size(max = 2000) String notes
) {
}
