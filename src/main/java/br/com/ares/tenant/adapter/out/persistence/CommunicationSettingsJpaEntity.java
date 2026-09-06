package br.com.ares.tenant.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_communication_settings")
class CommunicationSettingsJpaEntity {

    @Id
    @Column(name = "tenant_id")
    UUID tenantId;
    @Column(name = "whatsapp_enabled", nullable = false)
    boolean whatsappEnabled;
    @Column(name = "whatsapp_number", length = 20)
    String whatsappNumber;
    @Column(name = "whatsapp_token")
    String whatsappToken;
    @Column(name = "smtp_enabled", nullable = false)
    boolean smtpEnabled;
    @Column(name = "smtp_host", length = 255)
    String smtpHost;
    @Column(name = "smtp_port")
    Integer smtpPort;
    @Column(name = "smtp_username", length = 254)
    String smtpUsername;
    @Column(name = "smtp_password")
    String smtpPassword;
    @Column(name = "smtp_from_email", length = 254)
    String smtpFromEmail;
    @Column(name = "smtp_start_tls", nullable = false)
    boolean smtpStartTls;
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    protected CommunicationSettingsJpaEntity() {
    }
}

