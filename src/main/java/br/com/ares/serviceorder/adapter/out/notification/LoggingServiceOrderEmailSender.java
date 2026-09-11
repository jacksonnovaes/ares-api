package br.com.ares.serviceorder.adapter.out.notification;

import br.com.ares.serviceorder.application.port.out.ServiceOrderEmailSender;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.out.CommunicationSettingsRepository;
import br.com.ares.tenant.domain.model.CommunicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Component
class LoggingServiceOrderEmailSender implements ServiceOrderEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingServiceOrderEmailSender.class);
    private static final String TIMEOUT_MILLIS = "10000";

    private final CommunicationSettingsRepository settingsRepository;

    LoggingServiceOrderEmailSender(CommunicationSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    public void send(EmailMessage message) {
        CommunicationSettings settings = settingsRepository.findByTenantId(message.tenantId())
                .filter(CommunicationSettings::smtpEnabled)
                .orElseThrow(() -> BusinessException.badRequest("smtp_not_configured",
                        "Configure e habilite o servidor SMTP da empresa antes de enviar e-mails."));
        validate(settings);

        try {
            JavaMailSender mailSender = createMailSender(settings);
            var mail = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mail, true, StandardCharsets.UTF_8.name());
            helper.setFrom(settings.smtpFromEmail());
            helper.setTo(message.recipient());
            helper.setSubject(message.subject());
            helper.setText(message.body());
            helper.addAttachment(message.attachmentFilename(),
                    new ByteArrayResource(message.attachmentContent()), message.attachmentContentType());

            mailSender.send(mail);
            LOGGER.info("Service-order email sent via SMTP for tenant {} to {}",
                    message.tenantId(), message.recipient());
        } catch (MessagingException | MailException exception) {
            LOGGER.error("SMTP delivery failed for tenant {} to {}: {}",
                    message.tenantId(), message.recipient(), exception.getMessage());
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "smtp_delivery_failed",
                    "O servidor SMTP não conseguiu enviar o e-mail. Verifique as credenciais e tente novamente.");
        }
    }

    @Override
    public String deliveryMode() {
        return "SMTP";
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

    private void validate(CommunicationSettings settings) {
        if (!hasText(settings.smtpHost()) || settings.smtpPort() == null
                || !hasText(settings.smtpUsername()) || !hasText(settings.smtpPassword())
                || !hasText(settings.smtpFromEmail())) {
            throw BusinessException.badRequest("smtp_configuration_incomplete",
                    "A configuração SMTP da empresa está incompleta.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
