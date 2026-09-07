package br.com.ares.tenant.application.port.in;

public interface CommunicationSettingsUseCase {

    CommunicationSettingsView get();

    CommunicationSettingsView update(UpdateCommunicationSettingsCommand command);

    record CommunicationSettingsView(boolean whatsappEnabled, String whatsappNumber,
                                     boolean whatsappTokenConfigured, boolean smtpEnabled,
                                     String smtpHost, Integer smtpPort, String smtpUsername,
                                     boolean smtpPasswordConfigured, String smtpFromEmail,
                                     boolean smtpStartTls) {
    }

    /**
     * A credential set to {@code null} keeps the currently stored value. An empty
     * credential clears it, which is only accepted while its channel is disabled.
     */
    record UpdateCommunicationSettingsCommand(boolean whatsappEnabled, String whatsappNumber,
                                              String whatsappToken, boolean smtpEnabled,
                                              String smtpHost, Integer smtpPort, String smtpUsername,
                                              String smtpPassword, String smtpFromEmail,
                                              boolean smtpStartTls) {
    }
}

