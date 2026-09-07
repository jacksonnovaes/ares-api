package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.CommunicationSettingsUseCase;
import br.com.ares.tenant.application.port.out.CommunicationSettingsRepository;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.CommunicationSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class CommunicationSettingsService implements CommunicationSettingsUseCase {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?"
                    + "(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$",
            Pattern.CASE_INSENSITIVE);

    private final CommunicationSettingsRepository repository;
    private final TenantRepository tenants;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public CommunicationSettingsService(CommunicationSettingsRepository repository, TenantRepository tenants,
                                        CurrentActorProvider currentActor, AuditLogPort audit, Clock clock) {
        this.repository = repository;
        this.tenants = tenants;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public CommunicationSettingsView get() {
        var tenantId = currentActor.requiredActor().tenantId();
        ensureTenantExists(tenantId);
        return repository.findByTenantId(tenantId).map(this::toView).orElseGet(this::emptyView);
    }

    @Override
    @Transactional
    public CommunicationSettingsView update(UpdateCommunicationSettingsCommand command) {
        var actor = currentActor.requiredActor();
        ensureTenantExists(actor.tenantId());
        CommunicationSettings current = repository.findByTenantId(actor.tenantId()).orElse(null);

        String whatsappNumber = normalizePhone(command.whatsappNumber());
        String whatsappToken = updatedCredential(command.whatsappToken(),
                current == null ? null : current.whatsappToken());
        String smtpHost = normalized(command.smtpHost());
        String smtpUsername = normalized(command.smtpUsername());
        String smtpPassword = updatedCredential(command.smtpPassword(),
                current == null ? null : current.smtpPassword());
        String smtpFromEmail = lowerCase(command.smtpFromEmail());

        validateWhatsapp(command.whatsappEnabled(), whatsappNumber, whatsappToken);
        validateSmtp(command.smtpEnabled(), smtpHost, command.smtpPort(), smtpUsername, smtpPassword,
                smtpFromEmail);

        var updated = repository.save(new CommunicationSettings(actor.tenantId(), command.whatsappEnabled(),
                whatsappNumber, whatsappToken, command.smtpEnabled(), smtpHost, command.smtpPort(),
                smtpUsername, smtpPassword, smtpFromEmail, command.smtpStartTls(), clock.instant()));

        var details = new LinkedHashMap<String, Object>();
        details.put("whatsappEnabled", updated.whatsappEnabled());
        details.put("whatsappNumber", updated.whatsappNumber());
        details.put("whatsappTokenConfigured", hasText(updated.whatsappToken()));
        details.put("smtpEnabled", updated.smtpEnabled());
        details.put("smtpHost", updated.smtpHost());
        details.put("smtpPort", updated.smtpPort());
        details.put("smtpUsername", updated.smtpUsername());
        details.put("smtpPasswordConfigured", hasText(updated.smtpPassword()));
        details.put("smtpFromEmail", updated.smtpFromEmail());
        details.put("smtpStartTls", updated.smtpStartTls());
        audit.record(actor.tenantId(), actor.userId(), "COMMUNICATION_SETTINGS_UPDATED", "TENANT",
                actor.tenantId().toString(), details);
        return toView(updated);
    }

    private void validateWhatsapp(boolean enabled, String number, String token) {
        if (number != null && (number.length() < 8 || number.length() > 20)) {
            throw BusinessException.badRequest("whatsapp_number_invalid",
                    "Informe um número de WhatsApp válido, incluindo o código do país.");
        }
        if (enabled && number == null) {
            throw BusinessException.badRequest("whatsapp_number_required",
                    "Informe o número para habilitar o WhatsApp.");
        }
        if (enabled && !hasText(token)) {
            throw BusinessException.badRequest("whatsapp_token_required",
                    "Informe o token para habilitar o WhatsApp.");
        }
    }

    private void validateSmtp(boolean enabled, String host, Integer port, String username, String password,
                              String fromEmail) {
        if (port != null && (port < 1 || port > 65535)) {
            throw BusinessException.badRequest("smtp_port_invalid", "Informe uma porta SMTP válida.");
        }
        if (fromEmail != null && !EMAIL.matcher(fromEmail).matches()) {
            throw BusinessException.badRequest("smtp_from_email_invalid", "Informe um e-mail remetente válido.");
        }
        if (!enabled) {
            return;
        }
        if (host == null || port == null) {
            throw BusinessException.badRequest("smtp_server_required",
                    "Informe o host e a porta para habilitar o SMTP.");
        }
        if (username == null || !hasText(password)) {
            throw BusinessException.badRequest("smtp_credentials_required",
                    "Informe o usuário e a senha para habilitar o SMTP.");
        }
        if (fromEmail == null) {
            throw BusinessException.badRequest("smtp_from_email_required",
                    "Informe o e-mail remetente para habilitar o SMTP.");
        }
    }

    private void ensureTenantExists(java.util.UUID tenantId) {
        if (tenants.findById(tenantId).isEmpty()) {
            throw BusinessException.notFound("tenant_not_found", "Empresa não encontrada.");
        }
    }

    private String normalizePhone(String value) {
        String normalized = normalized(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.replaceAll("\\D", "");
        return normalized.isEmpty() ? null : normalized;
    }

    private String lowerCase(String value) {
        String normalized = normalized(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalized(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String updatedCredential(String requested, String current) {
        return requested == null ? current : normalized(requested);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private CommunicationSettingsView toView(CommunicationSettings value) {
        return new CommunicationSettingsView(value.whatsappEnabled(), value.whatsappNumber(),
                hasText(value.whatsappToken()), value.smtpEnabled(), value.smtpHost(), value.smtpPort(),
                value.smtpUsername(), hasText(value.smtpPassword()), value.smtpFromEmail(),
                value.smtpStartTls());
    }

    private CommunicationSettingsView emptyView() {
        return new CommunicationSettingsView(false, null, false, false, null, null, null,
                false, null, true);
    }
}

