package br.com.ares.servicecatalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CatalogService(UUID id, UUID tenantId, String name, String description,
                             BigDecimal basePrice, Integer estimatedMinutes, CatalogServiceType type, boolean active,
                             Instant createdAt, Instant updatedAt) {
}
