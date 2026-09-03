package br.com.ares.serviceorder.domain.model;

import java.time.Instant;

public record ServiceOrderDelivery(
        Instant deliveredAt,
        String receivedBy,
        int warrantyDays,
        Instant warrantyUntil,
        String warrantyTerms,
        String notes
) {
}
