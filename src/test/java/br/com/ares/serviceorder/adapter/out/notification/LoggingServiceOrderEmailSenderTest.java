package br.com.ares.serviceorder.adapter.out.notification;

import br.com.ares.serviceorder.application.port.out.ServiceOrderEmailSender;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.out.CommunicationSettingsRepository;
import br.com.ares.tenant.domain.model.CommunicationSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingServiceOrderEmailSenderTest {

    @Mock CommunicationSettingsRepository settingsRepository;
    @Mock JavaMailSender mailSender;

    private LoggingServiceOrderEmailSender sender;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        sender = new LoggingServiceOrderEmailSender(settingsRepository) {
            @Override
            JavaMailSender createMailSender(CommunicationSettings settings) {
                return mailSender;
            }
        };
    }

    @Test
    void sendsMessageUsingTheTenantSmtpConfiguration() {
        when(settingsRepository.findByTenantId(tenantId)).thenReturn(Optional.of(settings(true)));

        sender.send(new ServiceOrderEmailSender.EmailMessage(tenantId, "customer@example.com",
                "Ordem de serviço", "Conteúdo do e-mail"));

        var message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertThat(message.getValue().getFrom()).isEqualTo("sender@example.com");
        assertThat(message.getValue().getTo()).containsExactly("customer@example.com");
        assertThat(message.getValue().getSubject()).isEqualTo("Ordem de serviço");
        assertThat(message.getValue().getText()).isEqualTo("Conteúdo do e-mail");
        assertThat(sender.deliveryMode()).isEqualTo("SMTP");
    }

    @Test
    void configuresAuthenticationStartTlsAndNetworkTimeouts() {
        var realSender = new LoggingServiceOrderEmailSender(settingsRepository);

        var mail = (JavaMailSenderImpl) realSender.createMailSender(settings(true));

        assertThat(mail.getHost()).isEqualTo("smtp.example.com");
        assertThat(mail.getPort()).isEqualTo(587);
        assertThat(mail.getUsername()).isEqualTo("mailer@example.com");
        assertThat(mail.getPassword()).isEqualTo("secret-password");
        assertThat(mail.getJavaMailProperties())
                .containsEntry("mail.smtp.auth", "true")
                .containsEntry("mail.smtp.starttls.enable", "true")
                .containsEntry("mail.smtp.starttls.required", "true")
                .containsEntry("mail.smtp.connectiontimeout", "10000")
                .containsEntry("mail.smtp.timeout", "10000")
                .containsEntry("mail.smtp.writetimeout", "10000");
    }

    @Test
    void rejectsSendingWhenSmtpIsNotEnabledForTheTenant() {
        when(settingsRepository.findByTenantId(tenantId)).thenReturn(Optional.of(settings(false)));

        assertThatThrownBy(() -> sender.send(new ServiceOrderEmailSender.EmailMessage(tenantId,
                "customer@example.com", "Assunto", "Mensagem")))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.code()).isEqualTo("smtp_not_configured");
                    assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void convertsMailServerFailuresToAUsefulApiError() {
        when(settingsRepository.findByTenantId(tenantId)).thenReturn(Optional.of(settings(true)));
        doThrow(new MailSendException("Authentication failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> sender.send(new ServiceOrderEmailSender.EmailMessage(tenantId,
                "customer@example.com", "Assunto", "Mensagem")))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.code()).isEqualTo("smtp_delivery_failed");
                    assertThat(exception.status()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(exception.getMessage()).doesNotContain("Authentication failed");
                });
    }

    private CommunicationSettings settings(boolean enabled) {
        return new CommunicationSettings(tenantId, false, null, null, enabled, "smtp.example.com", 587,
                "mailer@example.com", "secret-password", "sender@example.com", true, Instant.now());
    }
}

