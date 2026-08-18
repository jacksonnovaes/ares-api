package br.com.ares.identity.adapter.out.notification;

import br.com.ares.identity.application.port.out.PasswordResetNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!production")
class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);

    @Override
    public void send(String email, String name, String rawToken) {
        LOGGER.info("Password reset requested for {} ({}). Development token: {}", name, email, rawToken);
    }
}
