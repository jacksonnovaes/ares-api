package br.com.ares.identity.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

interface SpringDataRefreshSessionRepository extends JpaRepository<RefreshSessionJpaEntity, UUID> {
    Optional<RefreshSessionJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshSessionJpaEntity r set r.revokedAt = :at where r.familyId = :familyId and r.revokedAt is null")
    void revokeFamily(@Param("familyId") UUID familyId, @Param("at") Instant at);

    @Modifying
    @Query("update RefreshSessionJpaEntity r set r.revokedAt = :at where r.userId = :userId and r.revokedAt is null")
    void revokeAllByUserId(@Param("userId") UUID userId, @Param("at") Instant at);
}
