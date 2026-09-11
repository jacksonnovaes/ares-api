package br.com.ares.identity.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataOnboardingRepository extends JpaRepository<OnboardingJpaEntity, UUID> {
}
