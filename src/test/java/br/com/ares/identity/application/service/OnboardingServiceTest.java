package br.com.ares.identity.application.service;

import br.com.ares.identity.application.port.out.OnboardingRepository;
import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.CurrentActorProvider;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OnboardingServiceTest {

    private static final UUID USER_ID = UUID.fromString("12edcfb6-1ce0-4cf3-9371-458d558248b8");
    private static final Instant NOW = Instant.parse("2026-09-11T14:00:00Z");

    private final InMemoryOnboardingRepository repository = new InMemoryOnboardingRepository();
    private final CurrentActorProvider currentActor = () ->
            new AuthenticatedActor(USER_ID, UUID.randomUUID(), "usuario@aresapp.tech", Set.of(), Set.of(), null);
    private final OnboardingService service = new OnboardingService(
            repository, currentActor, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void returnsPendingStateWhenTourWasNotCompleted() {
        var state = service.getState();

        assertThat(state.completed()).isFalse();
        assertThat(state.completedAt()).isNull();
    }

    @Test
    void persistsCompletionForCurrentUser() {
        var state = service.complete();

        assertThat(state.completed()).isTrue();
        assertThat(state.completedAt()).isEqualTo(NOW);
        assertThat(repository.findCompletedAt(USER_ID)).contains(NOW);
    }

    private static final class InMemoryOnboardingRepository implements OnboardingRepository {
        private UUID userId;
        private Instant completedAt;

        @Override
        public Optional<Instant> findCompletedAt(UUID requestedUserId) {
            return requestedUserId.equals(userId) ? Optional.of(completedAt) : Optional.empty();
        }

        @Override
        public void markCompleted(UUID completedUserId, Instant completionTime) {
            userId = completedUserId;
            completedAt = completionTime;
        }
    }
}
