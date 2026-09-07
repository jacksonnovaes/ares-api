package br.com.ares.identity.adapter.out.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableMethodSecurity
class SecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    JwtEncoder jwtEncoder(@Value("${ares.security.secret-base64}") String encodedSecret) {
        SecretKey key = secretKey(encodedSecret);
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(key));
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${ares.security.secret-base64}") String encodedSecret,
                          @Value("${ares.security.issuer}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(encodedSecret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                            RestAuthenticationEntryPoint entryPoint,
                                            RestAccessDeniedHandler accessDeniedHandler,
                                            @Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled)
            throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(
                            "/api/v1/tenants/register",
                            "/api/v1/tenants/registration-config",
                            "/api/v1/tenants/plan-whatsapp-simulation",
                            "/api/v1/tenants/coupon-validation",
                            "/api/v1/branding",
                            "/api/v1/public/profiles/**",
                            "/api/v1/public/media/**",
                            "/api/v1/auth/login",
                            "/api/v1/customer/auth/login",
                            "/api/v1/auth/refresh",
                            "/api/v1/auth/logout",
                            "/api/v1/auth/forgot-password",
                            "/api/v1/auth/reset-password",
                            "/actuator/health/**",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html").permitAll();
                    if (h2ConsoleEnabled) {
                        authorize.requestMatchers("/h2-console/**").permitAll();
                    }
                    authorize.anyRequest().authenticated();
                })
                .headers(headers -> {
                    if (h2ConsoleEnabled) {
                        headers.frameOptions(frame -> frame.sameOrigin());
                    }
                })
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private SecretKey secretKey(String encodedSecret) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(encodedSecret.replaceAll("\\s", ""));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET_BASE64 must be valid Base64", exception);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET_BASE64 must contain at least 32 bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
