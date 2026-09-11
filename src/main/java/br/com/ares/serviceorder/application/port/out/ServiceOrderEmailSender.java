package br.com.ares.serviceorder.application.port.out;

import java.util.UUID;

public interface ServiceOrderEmailSender {

    void send(EmailMessage message);

    String deliveryMode();

    record EmailMessage(UUID tenantId, String recipient, String subject, String body,
                        String attachmentFilename, String attachmentContentType, byte[] attachmentContent) {
    }
}
