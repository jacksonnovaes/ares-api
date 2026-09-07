package br.com.ares.tenant.adapter.in.web;

import br.com.ares.tenant.application.port.in.CommunicationSettingsUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/communication-settings")
public class CommunicationSettingsController {

    private final CommunicationSettingsUseCase settings;

    public CommunicationSettingsController(CommunicationSettingsUseCase settings) {
        this.settings = settings;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    CommunicationSettingsUseCase.CommunicationSettingsView get() {
        return settings.get();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    CommunicationSettingsUseCase.CommunicationSettingsView update(
            @Valid @RequestBody UpdateCommunicationSettingsRequest request) {
        return settings.update(new CommunicationSettingsUseCase.UpdateCommunicationSettingsCommand(
                request.whatsappEnabled(), request.whatsappNumber(), request.whatsappToken(),
                request.smtpEnabled(), request.smtpHost(), request.smtpPort(), request.smtpUsername(),
                request.smtpPassword(), request.smtpFromEmail(), request.smtpStartTls()));
    }

    record UpdateCommunicationSettingsRequest(
            @NotNull Boolean whatsappEnabled,
            @Size(max = 30) String whatsappNumber,
            @Size(max = 4096) String whatsappToken,
            @NotNull Boolean smtpEnabled,
            @Size(max = 255) String smtpHost,
            @Min(1) @Max(65535) Integer smtpPort,
            @Size(max = 254) String smtpUsername,
            @Size(max = 4096) String smtpPassword,
            @Email @Size(max = 254) String smtpFromEmail,
            @NotNull Boolean smtpStartTls) {
    }
}

