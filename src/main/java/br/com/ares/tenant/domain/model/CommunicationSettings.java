package br.com.ares.tenant.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CommunicationSettings(
        UUID tenantId,
        boolean whatsappEnabled,
        String whatsappNumber,
        String whatsappToken,
        boolean smtpEnabled,
        String smtpHost,
        Integer smtpPort,
        String smtpUsername,
        String smtpPassword,
        String smtpFromEmail,
        boolean smtpStartTls,
        Instant updatedAt
) {
}

