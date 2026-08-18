package br.com.ares.identity.adapter.out.security;

import br.com.ares.identity.application.port.out.TokenService;
import br.com.ares.identity.domain.model.User;
import br.com.ares.shared.domain.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Component
class JwtTokenAdapter implements TokenService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final Clock clock;
    private final String issuer;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    JwtTokenAdapter(JwtEncoder encoder, JwtDecoder decoder, Clock clock,
                    @Value("${ares.security.issuer}") String issuer,
                    @Value("${ares.security.access-token-ttl}") Duration accessTtl,
                    @Value("${ares.security.refresh-token-ttl}") Duration refreshTtl) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.clock = clock;
        this.issuer = issuer;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    @Override
    public String createAccessToken(User user) {
        Instant now = clock.instant();
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.id().toString())
                .issuedAt(now)
                .expiresAt(now.plus(accessTtl))
                .id(UUID.randomUUID().toString())
                .claim("type", "access")
                .claim("tenant_id", user.tenantId().toString())
                .claim("email", user.email())
                .claim("roles", user.roles().stream().map(Enum::name).sorted().toList())
                .claim("permissions", user.permissions().stream().map(Enum::name).sorted().toList())
                .build();
        return encode(claims);
    }

    @Override
    public IssuedRefreshToken createRefreshToken(User user, UUID familyId) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(refreshTtl);
        UUID tokenId = UUID.randomUUID();
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.id().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(tokenId.toString())
                .claim("type", "refresh")
                .claim("tenant_id", user.tenantId().toString())
                .claim("family_id", familyId.toString())
                .build();
        return new IssuedRefreshToken(encode(claims), tokenId, expiresAt);
    }

    @Override
    public RefreshClaims decodeRefreshToken(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            if (!"refresh".equals(jwt.getClaimAsString("type"))) {
                throw new IllegalArgumentException("Unexpected token type");
            }
            return new RefreshClaims(UUID.fromString(jwt.getId()), UUID.fromString(jwt.getSubject()),
                    UUID.fromString(jwt.getClaimAsString("tenant_id")), jwt.getExpiresAt());
        } catch (JwtException | IllegalArgumentException exception) {
            throw BusinessException.unauthorized("invalid_refresh_token", "Refresh token inválido ou expirado.");
        }
    }

    @Override
    public String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private String encode(JwtClaimsSet claims) {
        var header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
