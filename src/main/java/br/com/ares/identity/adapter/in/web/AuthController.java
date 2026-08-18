package br.com.ares.identity.adapter.in.web;

import br.com.ares.identity.application.port.in.AuthUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthUseCase auth;

    public AuthController(AuthUseCase auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    AuthUseCase.AuthenticationResult login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        return auth.authenticate(new AuthUseCase.LoginCommand(body.email(), body.password(), request.getRemoteAddr()));
    }

    @PostMapping("/refresh")
    AuthUseCase.AuthenticationResult refresh(@Valid @RequestBody TokenRequest body) {
        return auth.refresh(body.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@Valid @RequestBody TokenRequest body) {
        auth.logout(body.refreshToken());
    }

    @PostMapping("/forgot-password")
    ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest body) {
        auth.forgotPassword(body.email());
        return ResponseEntity.accepted().body(Map.of("message",
                "Se o e-mail estiver cadastrado, as instruções serão enviadas."));
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void resetPassword(@Valid @RequestBody ResetPasswordRequest body) {
        auth.resetPassword(new AuthUseCase.ResetPasswordCommand(
                body.token(), body.newPassword(), body.passwordConfirmation()));
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(@Valid @RequestBody ChangePasswordRequest body) {
        auth.changePassword(new AuthUseCase.ChangePasswordCommand(
                body.currentPassword(), body.newPassword(), body.passwordConfirmation()));
    }

    @GetMapping("/me")
    AuthUseCase.MeResult me() {
        return auth.me();
    }

    record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    record TokenRequest(@NotBlank String refreshToken) {
    }

    record ForgotPasswordRequest(@NotBlank @Email String email) {
    }

    record ResetPasswordRequest(@NotBlank String token, @NotBlank String newPassword,
                                @NotBlank String passwordConfirmation) {
    }

    record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword,
                                 @NotBlank String passwordConfirmation) {
    }
}
