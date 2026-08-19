package br.com.ares.identity.adapter.in.web;

import br.com.ares.identity.application.port.in.AuthUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/auth")
public class CustomerAuthController {

    private final AuthUseCase auth;

    public CustomerAuthController(AuthUseCase auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    AuthUseCase.AuthenticationResult login(@Valid @RequestBody LoginRequest body,
                                           HttpServletRequest request) {
        return auth.authenticateCustomer(new AuthUseCase.LoginCommand(
                body.email(), body.password(), request.getRemoteAddr()));
    }

    record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }
}
