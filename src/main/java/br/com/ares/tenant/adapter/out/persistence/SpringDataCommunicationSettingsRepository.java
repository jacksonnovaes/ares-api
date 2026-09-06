package br.com.ares.tenant.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataCommunicationSettingsRepository
        extends JpaRepository<CommunicationSettingsJpaEntity, UUID> {
}

