package br.com.ares.identity.domain.service;

import br.com.ares.shared.domain.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {

    public void validate(String password) {
        if (password == null || password.length() < 12 || password.length() > 72
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isDigit)
                || password.chars().allMatch(Character::isLetterOrDigit)) {
            throw BusinessException.badRequest("weak_password",
                    "A senha deve ter entre 12 e 72 caracteres, com maiúscula, minúscula, número e símbolo.");
        }
    }

    public void requireConfirmation(String password, String confirmation) {
        if (password == null || !password.equals(confirmation)) {
            throw BusinessException.badRequest("password_confirmation_mismatch",
                    "A confirmação da senha não confere.");
        }
    }
}
