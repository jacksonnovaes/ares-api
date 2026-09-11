package br.com.ares.identity.adapter.in.web;

import br.com.ares.identity.application.port.in.OnboardingUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final OnboardingUseCase onboarding;

    public OnboardingController(OnboardingUseCase onboarding) {
        this.onboarding = onboarding;
    }

    @GetMapping
    OnboardingUseCase.OnboardingState getState() {
        return onboarding.getState();
    }

    @PutMapping
    OnboardingUseCase.OnboardingState complete() {
        return onboarding.complete();
    }
}
