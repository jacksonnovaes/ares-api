package br.com.ares.identity.application.service;

import br.com.ares.identity.application.port.in.OnboardingUseCase;
import br.com.ares.identity.application.port.out.OnboardingRepository;
import br.com.ares.shared.application.CurrentActorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class OnboardingService implements OnboardingUseCase {

    private final OnboardingRepository onboarding;
    private final CurrentActorProvider currentActor;
    private final Clock clock;

    public OnboardingService(OnboardingRepository onboarding, CurrentActorProvider currentActor, Clock clock) {
        this.onboarding = onboarding;
        this.currentActor = currentActor;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public OnboardingState getState() {
        return onboarding.findCompletedAt(currentActor.requiredActor().userId())
                .map(completedAt -> new OnboardingState(true, completedAt))
                .orElseGet(() -> new OnboardingState(false, null));
    }

    @Override
    @Transactional
    public OnboardingState complete() {
        Instant completedAt = clock.instant();
        onboarding.markCompleted(currentActor.requiredActor().userId(), completedAt);
        return new OnboardingState(true, completedAt);
    }
}
