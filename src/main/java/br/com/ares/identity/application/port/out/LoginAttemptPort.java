package br.com.ares.identity.application.port.out;

public interface LoginAttemptPort {
    boolean isBlocked(String key);
    void failed(String key);
    void succeeded(String key);
}
