package br.com.ares.identity.application.port.out;

public interface PasswordResetNotifier {
    void send(String email, String name, String rawToken);
}
