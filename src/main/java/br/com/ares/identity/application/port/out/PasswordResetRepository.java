package br.com.ares.identity.application.port.out;

import br.com.ares.identity.domain.model.PasswordReset;

import java.util.Optional;

public interface PasswordResetRepository {
    PasswordReset save(PasswordReset reset);
    Optional<PasswordReset> findByTokenHash(String tokenHash);
}
