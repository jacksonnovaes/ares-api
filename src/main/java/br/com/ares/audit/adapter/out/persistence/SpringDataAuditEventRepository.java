package br.com.ares.audit.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataAuditEventRepository extends JpaRepository<AuditEventJpaEntity, UUID> {
}
