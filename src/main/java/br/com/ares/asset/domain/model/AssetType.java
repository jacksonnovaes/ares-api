package br.com.ares.asset.domain.model;

import java.time.Instant;
import java.util.UUID;

public record AssetType(UUID id, UUID tenantId, String code, String name, boolean systemDefault,
                        boolean active, Instant createdAt, Instant updatedAt) {
}
