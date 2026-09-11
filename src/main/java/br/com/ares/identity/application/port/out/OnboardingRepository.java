package br.com.ares.identity.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OnboardingRepository {

    Optional<Instant> findCompletedAt(UUID userId);

    void markCompleted(UUID userId, Instant completedAt);
}
