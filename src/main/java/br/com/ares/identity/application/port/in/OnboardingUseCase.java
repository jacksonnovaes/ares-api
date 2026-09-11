package br.com.ares.identity.application.port.in;

import java.time.Instant;

public interface OnboardingUseCase {

    OnboardingState getState();

    OnboardingState complete();

    record OnboardingState(boolean completed, Instant completedAt) {
    }
}
