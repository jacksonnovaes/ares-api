package br.com.ares.asset.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record Asset(UUID id, UUID tenantId, UUID customerId, String type, String name,
                    String brand, String model, String serialNumber, Map<String, String> attributes,
                    Instant createdAt, Instant updatedAt) {
}
