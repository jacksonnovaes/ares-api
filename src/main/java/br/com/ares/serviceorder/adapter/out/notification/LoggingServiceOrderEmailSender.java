package br.com.ares.serviceorder.adapter.out.notification;

import br.com.ares.serviceorder.application.port.out.ServiceOrderEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingServiceOrderEmailSender implements ServiceOrderEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingServiceOrderEmailSender.class);

    @Override
    public void send(EmailMessage message) {
        LOGGER.info("Simulated service-order email to {} with subject '{}'. Body:\n{}",
                message.recipient(), message.subject(), message.body());
    }

    @Override
    public String deliveryMode() {
        return "SIMULATION";
    }
}
