package br.com.ares.identity.adapter.in.web;

import br.com.ares.identity.application.port.in.RegistrationUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
public class RegistrationController {

    private final RegistrationUseCase registration;

    public RegistrationController(RegistrationUseCase registration) {
        this.registration = registration;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        var result = registration.register(new RegistrationUseCase.RegisterTenantAdminCommand(
                request.legalName(), request.tradeName(), request.slug(), request.document(),
                request.logoUrl(), request.primaryColor(), request.admin().name(), request.admin().email(),
                request.admin().password(), request.admin().passwordConfirmation()));
        return new RegistrationResponse(result.tenantId(), result.userId(), result.slug());
    }

    record RegistrationRequest(
            @NotBlank @Size(max = 160) String legalName,
            @NotBlank @Size(max = 160) String tradeName,
            @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 80) String slug,
            @NotBlank
            @Pattern(regexp = "(?:\\d{11}|\\d{14})",
                    message = "Informe um CPF com 11 dígitos ou um CNPJ com 14 dígitos.")
            String document,
            @Size(max = 500) String logoUrl,
            @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String primaryColor,
            @NotNull @Valid AdminRequest admin
    ) {
    }

    record AdminRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank String password,
            @NotBlank String passwordConfirmation
    ) {
    }

    record RegistrationResponse(UUID tenantId, UUID userId, String slug) {
    }
}
