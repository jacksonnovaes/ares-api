package br.com.ares.identity.application.port.in;

import br.com.ares.identity.domain.model.Permission;
import br.com.ares.identity.domain.model.Role;

import java.util.Set;
import java.util.UUID;

public interface AuthUseCase {
    AuthenticationResult authenticate(LoginCommand command);
    AuthenticationResult authenticateCustomer(LoginCommand command);
    AuthenticationResult refresh(String refreshToken);
    void logout(String refreshToken);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordCommand command);
    void changePassword(ChangePasswordCommand command);
    MeResult me();

    record LoginCommand(String email, String password, String clientKey) {
    }

    record ResetPasswordCommand(String token, String newPassword, String passwordConfirmation) {
    }

    record ChangePasswordCommand(String currentPassword, String newPassword, String passwordConfirmation) {
    }

    record AuthenticationResult(String accessToken, String refreshToken, long expiresIn,
                                AuthenticatedUser user) {
    }

    record AuthenticatedUser(UUID id, String name, UUID tenantId, Set<Role> roles) {
    }

    record MeResult(UUID id, String name, String email, TenantSummary tenant,
                    Set<Role> roles, Set<Permission> permissions) {
    }

    record TenantSummary(UUID id, String name, String slug) {
    }
}
