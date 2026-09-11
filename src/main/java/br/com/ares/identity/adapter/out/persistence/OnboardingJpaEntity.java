package br.com.ares.identity.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_onboarding")
class OnboardingJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Column(name = "completed_at", nullable = false)
    Instant completedAt;

    protected OnboardingJpaEntity() {
    }

    OnboardingJpaEntity(UUID userId, Instant completedAt) {
        this.userId = userId;
        this.completedAt = completedAt;
    }
}
