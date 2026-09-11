package br.com.ares.identity.adapter.out.persistence;

import br.com.ares.identity.application.port.out.OnboardingRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
class OnboardingPersistenceAdapter implements OnboardingRepository {

    private final SpringDataOnboardingRepository repository;

    OnboardingPersistenceAdapter(SpringDataOnboardingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Instant> findCompletedAt(UUID userId) {
        return repository.findById(userId).map(entity -> entity.completedAt);
    }

    @Override
    public void markCompleted(UUID userId, Instant completedAt) {
        repository.save(new OnboardingJpaEntity(userId, completedAt));
    }
}
