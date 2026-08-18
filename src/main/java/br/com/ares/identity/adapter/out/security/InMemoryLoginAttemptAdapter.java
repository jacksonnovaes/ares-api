package br.com.ares.identity.adapter.out.security;

import br.com.ares.identity.application.port.out.LoginAttemptPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
class InMemoryLoginAttemptAdapter implements LoginAttemptPort {

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int maxFailures;
    private final Duration window;
    private final Duration blockDuration;

    InMemoryLoginAttemptAdapter(Clock clock,
                                @Value("${ares.security.login.max-failures}") int maxFailures,
                                @Value("${ares.security.login.window}") Duration window,
                                @Value("${ares.security.login.block-duration}") Duration blockDuration) {
        this.clock = clock;
        this.maxFailures = maxFailures;
        this.window = window;
        this.blockDuration = blockDuration;
    }

    @Override
    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) return false;
        Instant now = clock.instant();
        if (attempt.blockedUntil() != null && attempt.blockedUntil().isAfter(now)) return true;
        if (attempt.startedAt().plus(window).isBefore(now)) attempts.remove(key, attempt);
        return false;
    }

    @Override
    public void failed(String key) {
        Instant now = clock.instant();
        attempts.compute(key, (ignored, current) -> {
            if (current == null || current.startedAt().plus(window).isBefore(now)) {
                return new Attempt(1, now, null);
            }
            int failures = current.failures() + 1;
            Instant blockedUntil = failures >= maxFailures ? now.plus(blockDuration) : current.blockedUntil();
            return new Attempt(failures, current.startedAt(), blockedUntil);
        });
    }

    @Override
    public void succeeded(String key) {
        attempts.remove(key);
    }

    private record Attempt(int failures, Instant startedAt, Instant blockedUntil) {
    }
}
