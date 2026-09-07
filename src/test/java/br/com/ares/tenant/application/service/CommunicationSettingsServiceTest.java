package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.CommunicationSettingsUseCase;
import br.com.ares.tenant.application.port.out.CommunicationSettingsRepository;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.CommunicationSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunicationSettingsServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-06T12:00:00Z");

    @Mock CommunicationSettingsRepository repository;
    @Mock TenantRepository tenants;
    @Mock AuditLogPort audit;

    private CommunicationSettingsService service;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        var actor = new AuthenticatedActor(UUID.randomUUID(), tenantId, "admin@example.com",
                Set.of("ADMIN"), Set.of("TENANT_CONFIGURE"), null);
        CurrentActorProvider currentActor = () -> actor;
        service = new CommunicationSettingsService(repository, tenants, currentActor, audit,
                Clock.fixed(NOW, ZoneOffset.UTC));
        when(tenants.findById(tenantId)).thenReturn(Optional.of(org.mockito.Mockito.mock(
                br.com.ares.tenant.domain.model.Tenant.class)));
    }

    @Test
    void returnsDisabledDefaultsWhenTenantHasNoConfiguration() {
        when(repository.findByTenantId(tenantId)).thenReturn(Optional.empty());

        var result = service.get();

        assertThat(result.whatsappEnabled()).isFalse();
        assertThat(result.whatsappTokenConfigured()).isFalse();
        assertThat(result.smtpEnabled()).isFalse();
        assertThat(result.smtpPasswordConfigured()).isFalse();
        assertThat(result.smtpStartTls()).isTrue();
    }

    @Test
    void updatesSettingsWhileKeepingOmittedCredentialsAndNeverReturnsTheirValues() {
        var current = new CommunicationSettings(tenantId, true, "5511999999999", "secret-token",
                true, "smtp.old.example", 587, "old@example.com", "secret-password",
                "old@example.com", true, NOW.minusSeconds(60));
        when(repository.findByTenantId(tenantId)).thenReturn(Optional.of(current));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.update(new CommunicationSettingsUseCase.UpdateCommunicationSettingsCommand(
                true, "+55 (11) 98888-7777", null, true, "smtp.example.com", 587,
                "mailer@example.com", null, "Contato@Example.com", true));

        var saved = ArgumentCaptor.forClass(CommunicationSettings.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().whatsappNumber()).isEqualTo("5511988887777");
        assertThat(saved.getValue().whatsappToken()).isEqualTo("secret-token");
        assertThat(saved.getValue().smtpPassword()).isEqualTo("secret-password");
        assertThat(saved.getValue().smtpFromEmail()).isEqualTo("contato@example.com");
        assertThat(result.whatsappTokenConfigured()).isTrue();
        assertThat(result.smtpPasswordConfigured()).isTrue();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> auditDetails = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(tenantId), any(), eq("COMMUNICATION_SETTINGS_UPDATED"), eq("TENANT"),
                eq(tenantId.toString()), auditDetails.capture());
        assertThat(auditDetails.getValue()).doesNotContainValue("secret-token")
                .doesNotContainValue("secret-password");
    }

    @Test
    void requiresNumberAndTokenWhenWhatsappIsEnabled() {
        when(repository.findByTenantId(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(
                new CommunicationSettingsUseCase.UpdateCommunicationSettingsCommand(true, null, null,
                        false, null, null, null, null, null, true)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.code()).isEqualTo("whatsapp_number_required"));
    }

    @Test
    void requiresCompleteSmtpConfigurationWhenEnabled() {
        when(repository.findByTenantId(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(
                new CommunicationSettingsUseCase.UpdateCommunicationSettingsCommand(false, null, null,
                        true, "smtp.example.com", 587, null, null, "sender@example.com", true)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.code()).isEqualTo("smtp_credentials_required"));
    }
}

