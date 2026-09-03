package br.com.ares.tenant.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PublicProfileStoredMedia(
        UUID id,
        UUID tenantId,
        PublicProfileMediaKind kind,
        String filename,
        String contentType,
        byte[] content,
        int sizeBytes,
        Instant createdAt,
        Instant updatedAt
) {
}

