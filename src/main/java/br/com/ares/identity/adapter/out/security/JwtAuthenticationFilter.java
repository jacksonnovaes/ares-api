package br.com.ares.identity.adapter.out.security;

import br.com.ares.identity.application.service.IdentityAccessService;
import br.com.ares.identity.domain.model.User;
import br.com.ares.shared.domain.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/tenants/register",
            "/api/v1/tenants/registration-config",
            "/api/v1/tenants/plan-whatsapp-simulation",
            "/api/v1/tenants/coupon-validation",
            "/api/v1/branding",
            "/api/v1/auth/login",
            "/api/v1/customer/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    );

    private final JwtDecoder decoder;
    private final IdentityAccessService identityAccess;
    private final RestAuthenticationEntryPoint entryPoint;

    JwtAuthenticationFilter(JwtDecoder decoder, IdentityAccessService identityAccess,
                            RestAuthenticationEntryPoint entryPoint) {
        this.decoder = decoder;
        this.identityAccess = identityAccess;
        this.entryPoint = entryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.contains(path)
                || path.startsWith("/actuator/health/")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/swagger-ui/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Jwt jwt = decoder.decode(authorization.substring(7));
            if (!"access".equals(jwt.getClaimAsString("type"))) {
                throw BusinessException.unauthorized("invalid_token", "Token inválido.");
            }
            UUID userId = UUID.fromString(jwt.getSubject());
            UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
            User user = identityAccess.validate(userId, tenantId);
            var roles = user.roles().stream().map(Enum::name).collect(java.util.stream.Collectors.toUnmodifiableSet());
            var permissions = user.permissions().stream().map(Enum::name)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            var authorities = new ArrayList<SimpleGrantedAuthority>();
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
            var principal = new AuthenticatedPrincipal(user.id(), user.tenantId(), user.email(), roles,
                    permissions, user.customerId());
            var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException | BusinessException exception) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response, null);
        }
    }
}
