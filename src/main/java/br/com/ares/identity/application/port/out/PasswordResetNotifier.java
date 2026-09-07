package br.com.ares.identity.application.port.out;

import java.util.UUID;

public interface PasswordResetNotifier {
    void send(UUID tenantId, String tenantName, String email, String name, String rawToken);
}
