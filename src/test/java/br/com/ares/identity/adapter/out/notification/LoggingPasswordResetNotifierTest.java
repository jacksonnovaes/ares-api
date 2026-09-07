package br.com.ares.identity.adapter.out.notification;

import br.com.ares.tenant.application.port.out.CommunicationSettingsRepository;
import br.com.ares.tenant.domain.model.CommunicationSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingPasswordResetNotifierTest {

    @Mock CommunicationSettingsRepository settingsRepository;
    @Mock JavaMailSender mailSender;

    private LoggingPasswordResetNotifier notifier;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        notifier = new LoggingPasswordResetNotifier(settingsRepository, "https://app.ares.example/") {
            @Override
            JavaMailSender createMailSender(CommunicationSettings settings) {
                return mailSender;
            }
        };
    }

    @Test
    void sendsAResetLinkUsingTheTenantSmtpConfiguration() {
        when(settingsRepository.findByTenantId(tenantId)).thenReturn(Optional.of(settings(true)));

        notifier.send(tenantId, "Oficina Ares", "admin@example.com", "Maria", "token-safe_123");

        var message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertThat(message.getValue().getFrom()).isEqualTo("sender@example.com");
        assertThat(message.getValue().getTo()).containsExactly("admin@example.com");
        assertThat(message.getValue().getSubject()).isEqualTo("Recuperação de senha — Oficina Ares");
        assertThat(message.getValue().getText())
                .contains("Olá, Maria!", "https://app.ares.example/redefinir-senha?token=token-safe_123")
                .doesNotContain("smtp-password");
    }

    @Test
    void doesNotSendWhenTenantSmtpIsDisabled() {
        when(settingsRepository.findByTenantId(tenantId)).thenReturn(Optional.of(settings(false)));

        notifier.send(tenantId, "Oficina Ares", "admin@example.com", "Maria", "token");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void hidesDeliveryFailureToPreventAccountEnumeration() {
        when(settingsRepository.findByTenantId(tenantId)).thenReturn(Optional.of(settings(true)));
        doThrow(new MailSendException("Authentication failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> notifier.send(tenantId, "Oficina Ares", "admin@example.com", "Maria", "token"))
                .doesNotThrowAnyException();
    }

    private CommunicationSettings settings(boolean enabled) {
        return new CommunicationSettings(tenantId, false, null, null, enabled, "smtp.example.com", 587,
                "mailer@example.com", "smtp-password", "sender@example.com", true, Instant.now());
    }
}

