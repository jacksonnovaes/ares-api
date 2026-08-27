package br.com.ares.audit.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataAuditEventRepository extends JpaRepository<AuditEventJpaEntity, UUID> {
    List<AuditEventJpaEntity> findAllByTenantIdOrderByOccurredAtAsc(UUID tenantId);
}
