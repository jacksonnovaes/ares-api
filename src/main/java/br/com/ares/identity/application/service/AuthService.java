package br.com.ares.identity.application.service;

import br.com.ares.identity.application.port.in.AuthUseCase;
import br.com.ares.identity.application.port.out.*;
import br.com.ares.identity.domain.model.PasswordReset;
import br.com.ares.identity.domain.model.RefreshSession;
import br.com.ares.identity.domain.model.Role;
import br.com.ares.identity.domain.model.User;
import br.com.ares.identity.domain.service.PasswordPolicy;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService implements AuthUseCase {

    private static final String INVALID_CREDENTIALS = "E-mail ou senha inválidos.";

    private final UserRepository users;
    private final RefreshSessionRepository sessions;
    private final PasswordResetRepository passwordResets;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final TokenService tokens;
    private final PasswordResetNotifier notifier;
    private final LoginAttemptPort loginAttempts;
    private final TenantManagementUseCase tenants;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;
    private final Duration accessTokenTtl;
    private final Duration passwordResetTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository users, RefreshSessionRepository sessions,
                       PasswordResetRepository passwordResets, PasswordHasher passwordHasher,
                       PasswordPolicy passwordPolicy, TokenService tokens, PasswordResetNotifier notifier,
                       LoginAttemptPort loginAttempts, TenantManagementUseCase tenants,
                       CurrentActorProvider currentActor, AuditLogPort audit, Clock clock,
                       @Value("${ares.security.access-token-ttl}") Duration accessTokenTtl,
                       @Value("${ares.security.password-reset-ttl}") Duration passwordResetTtl) {
        this.users = users;
        this.sessions = sessions;
        this.passwordResets = passwordResets;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.tokens = tokens;
        this.notifier = notifier;
        this.loginAttempts = loginAttempts;
        this.tenants = tenants;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
        this.accessTokenTtl = accessTokenTtl;
        this.passwordResetTtl = passwordResetTtl;
    }

    @Override
    @Transactional
    public AuthenticationResult authenticate(LoginCommand command) {
        return authenticate(command, false);
    }

    @Override
    @Transactional
    public AuthenticationResult authenticateCustomer(LoginCommand command) {
        return authenticate(command, true);
    }

    private AuthenticationResult authenticate(LoginCommand command, boolean customerOnly) {
        String email = normalizeEmail(command.email());
        String attemptKey = email + "|" + command.clientKey();
        if (loginAttempts.isBlocked(attemptKey)) {
            throw BusinessException.tooManyRequests("login_rate_limited",
                    "Muitas tentativas. Aguarde antes de tentar novamente.");
        }
        User user = users.findByEmail(email).orElse(null);
        if (user == null || !passwordHasher.matches(command.password(), user.passwordHash())) {
            loginAttempts.failed(attemptKey);
            throw BusinessException.unauthorized("invalid_credentials", INVALID_CREDENTIALS);
        }
        if (customerOnly && (!user.roles().contains(Role.CUSTOMER) || user.customerId() == null)) {
            loginAttempts.failed(attemptKey);
            throw BusinessException.unauthorized("invalid_credentials", INVALID_CREDENTIALS);
        }
        validateAccess(user);
        loginAttempts.succeeded(attemptKey);
        user = users.save(user.withLastLogin(clock.instant()));
        audit.record(user.tenantId(), user.id(), "LOGIN_SUCCEEDED", "USER", user.id().toString(), Map.of());
        return issueAuthentication(user, UUID.randomUUID());
    }

    @Override
    @Transactional
    public AuthenticationResult refresh(String refreshToken) {
        TokenService.RefreshClaims claims = tokens.decodeRefreshToken(refreshToken);
        Instant now = clock.instant();
        RefreshSession current = sessions.findByTokenHash(tokens.hash(refreshToken)).orElseThrow(() ->
                BusinessException.unauthorized("invalid_refresh_token", "Refresh token inválido."));
        if (current.revokedAt() != null) {
            sessions.revokeFamily(current.familyId(), now);
            throw BusinessException.unauthorized("refresh_token_reused",
                    "Reutilização de refresh token detectada; a sessão foi encerrada.");
        }
        if (!current.isActiveAt(now)
                || !current.id().equals(claims.tokenId())
                || !current.userId().equals(claims.userId())
                || !current.tenantId().equals(claims.tenantId())) {
            throw BusinessException.unauthorized("invalid_refresh_token", "Refresh token inválido ou expirado.");
        }
        User user = users.findByIdAndTenantId(claims.userId(), claims.tenantId()).orElseThrow(() ->
                BusinessException.unauthorized("invalid_refresh_token", "Refresh token inválido."));
        validateAccess(user);

        TokenService.IssuedRefreshToken next = tokens.createRefreshToken(user, current.familyId());
        sessions.save(current.revoke(now, next.id()));
        sessions.save(new RefreshSession(next.id(), current.familyId(), user.id(), user.tenantId(),
                tokens.hash(next.value()), next.expiresAt(), null, null, now));
        return result(user, tokens.createAccessToken(user), next.value());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        tokens.decodeRefreshToken(refreshToken);
        sessions.findByTokenHash(tokens.hash(refreshToken)).ifPresent(session -> {
            if (session.revokedAt() == null) sessions.save(session.revoke(clock.instant(), null));
            audit.record(session.tenantId(), session.userId(), "LOGOUT", "USER",
                    session.userId().toString(), Map.of());
        });
    }

    @Override
    @Transactional
    public void forgotPassword(String emailValue) {
        String email = normalizeEmail(emailValue);
        users.findByEmail(email).ifPresent(user -> {
            if (!user.canAuthenticate()) return;
            var tenant = tenants.requiredById(user.tenantId());
            if (!tenant.isActive()) return;
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            Instant now = clock.instant();
            passwordResets.save(new PasswordReset(UUID.randomUUID(), user.id(), user.tenantId(),
                    tokens.hash(rawToken), now.plus(passwordResetTtl), null, now));
            notifier.send(user.email(), user.name(), rawToken);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordCommand command) {
        passwordPolicy.requireConfirmation(command.newPassword(), command.passwordConfirmation());
        passwordPolicy.validate(command.newPassword());
        Instant now = clock.instant();
        PasswordReset reset = passwordResets.findByTokenHash(tokens.hash(command.token())).orElseThrow(() ->
                BusinessException.badRequest("invalid_reset_token", "Token de recuperação inválido ou expirado."));
        if (!reset.isUsableAt(now)) {
            throw BusinessException.badRequest("invalid_reset_token", "Token de recuperação inválido ou expirado.");
        }
        User user = users.findByIdAndTenantId(reset.userId(), reset.tenantId()).orElseThrow(() ->
                BusinessException.badRequest("invalid_reset_token", "Token de recuperação inválido ou expirado."));
        users.save(user.withPassword(passwordHasher.hash(command.newPassword()), now));
        passwordResets.save(reset.markUsed(now));
        sessions.revokeAllByUserId(user.id(), now);
        audit.record(user.tenantId(), user.id(), "PASSWORD_RESET", "USER", user.id().toString(), Map.of());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        passwordPolicy.requireConfirmation(command.newPassword(), command.passwordConfirmation());
        passwordPolicy.validate(command.newPassword());
        var actor = currentActor.requiredActor();
        User user = users.findByIdAndTenantId(actor.userId(), actor.tenantId()).orElseThrow(() ->
                BusinessException.unauthorized("user_not_found", "Usuário autenticado não encontrado."));
        if (!passwordHasher.matches(command.currentPassword(), user.passwordHash())) {
            throw BusinessException.badRequest("invalid_current_password", "A senha atual está incorreta.");
        }
        Instant now = clock.instant();
        users.save(user.withPassword(passwordHasher.hash(command.newPassword()), now));
        sessions.revokeAllByUserId(user.id(), now);
        audit.record(user.tenantId(), user.id(), "PASSWORD_CHANGED", "USER", user.id().toString(), Map.of());
    }

    @Override
    @Transactional(readOnly = true)
    public MeResult me() {
        var actor = currentActor.requiredActor();
        User user = users.findByIdAndTenantId(actor.userId(), actor.tenantId()).orElseThrow(() ->
                BusinessException.unauthorized("user_not_found", "Usuário autenticado não encontrado."));
        var tenant = tenants.requiredById(actor.tenantId());
        return new MeResult(user.id(), user.name(), user.email(),
                new TenantSummary(tenant.id(), tenant.tradeName(), tenant.slug()),
                user.roles(), user.permissions());
    }

    private AuthenticationResult issueAuthentication(User user, UUID familyId) {
        TokenService.IssuedRefreshToken refresh = tokens.createRefreshToken(user, familyId);
        Instant now = clock.instant();
        sessions.save(new RefreshSession(refresh.id(), familyId, user.id(), user.tenantId(),
                tokens.hash(refresh.value()), refresh.expiresAt(), null, null, now));
        return result(user, tokens.createAccessToken(user), refresh.value());
    }

    private AuthenticationResult result(User user, String accessToken, String refreshToken) {
        return new AuthenticationResult(accessToken, refreshToken, accessTokenTtl.toSeconds(),
                new AuthenticatedUser(user.id(), user.name(), user.tenantId(), user.roles()));
    }

    private void validateAccess(User user) {
        if (!user.canAuthenticate()) {
            throw BusinessException.unauthorized("account_unavailable", "Conta ou empresa indisponível.");
        }
        if (!tenants.requiredById(user.tenantId()).isActive()) {
            throw BusinessException.unauthorized("account_unavailable", "Conta ou empresa indisponível.");
        }
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
