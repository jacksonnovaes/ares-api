package br.com.ares.identity.application.port.in;

import java.util.UUID;

public interface RegistrationUseCase {
    RegistrationResult register(RegisterTenantAdminCommand command);

    record RegisterTenantAdminCommand(
            String legalName, String tradeName, String slug, String document,
            String logoUrl, String primaryColor,
            String adminName, String adminEmail, String password, String passwordConfirmation
    ) {
    }

    record RegistrationResult(UUID tenantId, UUID userId, String slug) {
    }
}
