package br.com.ares.identity.adapter.out.notification;

import br.com.ares.identity.application.port.out.PasswordResetNotifier;
import br.com.ares.tenant.application.port.out.CommunicationSettingsRepository;
import br.com.ares.tenant.domain.model.CommunicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

@Component
class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);
    private static final String TIMEOUT_MILLIS = "10000";

    private final CommunicationSettingsRepository settingsRepository;
    private final String webBaseUrl;

    LoggingPasswordResetNotifier(CommunicationSettingsRepository settingsRepository,
                                 @Value("${ares.web.base-url:http://localhost:3000}") String webBaseUrl) {
        this.settingsRepository = settingsRepository;
        this.webBaseUrl = webBaseUrl.replaceAll("/+$", "");
    }

    @Override
    public void send(UUID tenantId, String tenantName, String email, String name, String rawToken) {
        try {
            CommunicationSettings settings = settingsRepository.findByTenantId(tenantId)
                    .filter(CommunicationSettings::smtpEnabled)
                    .orElse(null);
            if (!isComplete(settings)) {
                LOGGER.warn("Password reset email not sent for tenant {} because SMTP is not configured", tenantId);
                return;
            }

            String resetUrl = webBaseUrl + "/redefinir-senha?token="
                    + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
            var message = new SimpleMailMessage();
            message.setFrom(settings.smtpFromEmail());
            message.setTo(email);
            message.setSubject("Recuperação de senha — " + tenantName);
            message.setText("Olá, " + name + "!\n\n"
                    + "Recebemos uma solicitação para redefinir sua senha na " + tenantName + ".\n\n"
                    + "Acesse o link abaixo para criar uma nova senha:\n"
                    + resetUrl + "\n\n"
                    + "Se você não solicitou a recuperação, ignore esta mensagem.\n"
                    + "Por segurança, o link possui prazo de validade.");

            createMailSender(settings).send(message);
            LOGGER.info("Password reset email sent via SMTP for tenant {} to {}", tenantId, email);
        } catch (RuntimeException exception) {
            // A resposta pública deve continuar genérica para não revelar se a conta existe.
            LOGGER.error("Password reset SMTP delivery failed for tenant {} to {} ({})",
                    tenantId, email, exception.getClass().getSimpleName());
        }
    }

    JavaMailSender createMailSender(CommunicationSettings settings) {
        var sender = new JavaMailSenderImpl();
        sender.setHost(settings.smtpHost());
        sender.setPort(settings.smtpPort());
        sender.setUsername(settings.smtpUsername());
        sender.setPassword(settings.smtpPassword());
        sender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        Properties properties = sender.getJavaMailProperties();
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", Boolean.toString(settings.smtpStartTls()));
        properties.setProperty("mail.smtp.starttls.required", Boolean.toString(settings.smtpStartTls()));
        properties.setProperty("mail.smtp.connectiontimeout", TIMEOUT_MILLIS);
        properties.setProperty("mail.smtp.timeout", TIMEOUT_MILLIS);
        properties.setProperty("mail.smtp.writetimeout", TIMEOUT_MILLIS);
        return sender;
    }

    private boolean isComplete(CommunicationSettings settings) {
        return settings != null && hasText(settings.smtpHost()) && settings.smtpPort() != null
                && hasText(settings.smtpUsername()) && hasText(settings.smtpPassword())
                && hasText(settings.smtpFromEmail());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
