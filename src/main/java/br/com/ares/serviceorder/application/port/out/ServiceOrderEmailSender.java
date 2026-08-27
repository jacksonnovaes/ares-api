package br.com.ares.serviceorder.application.port.out;

public interface ServiceOrderEmailSender {

    void send(EmailMessage message);

    String deliveryMode();

    record EmailMessage(String recipient, String subject, String body) {
    }
}
