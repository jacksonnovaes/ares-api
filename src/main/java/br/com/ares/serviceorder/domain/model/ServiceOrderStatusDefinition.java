package br.com.ares.serviceorder.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ServiceOrderStatusDefinition(
        UUID id,
        UUID tenantId,
        String code,
        String name,
        boolean systemDefault,
        boolean active,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
