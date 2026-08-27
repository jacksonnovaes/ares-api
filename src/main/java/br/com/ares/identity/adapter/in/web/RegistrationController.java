package br.com.ares.identity.adapter.in.web;

import br.com.ares.identity.application.port.in.RegistrationUseCase;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
public class RegistrationController {

    private final RegistrationUseCase registration;

    public RegistrationController(RegistrationUseCase registration) {
        this.registration = registration;
    }

    @GetMapping("/registration-config")
    RegistrationConfigurationResponse registrationConfiguration() {
        var result = registration.registrationConfiguration();
        return new RegistrationConfigurationResponse(result.subscriptionPaymentSimulationEnabled(),
                result.couponEnabled(), result.termsVersion(), result.privacyVersion());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    RegistrationResponse register(@Valid @RequestBody RegistrationRequest request, HttpServletRequest httpRequest) {
        var result = registration.register(new RegistrationUseCase.RegisterTenantAdminCommand(
                request.legalName(), request.tradeName(), request.slug(), request.document(),
                request.logoUrl(), request.primaryColor(), request.plan(), request.whatsapp(),
                request.couponCode(), request.simulatedPaymentApproved(), request.termsAccepted(),
                request.privacyNoticeAcknowledged(), request.termsVersion(), request.privacyVersion(),
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"),
                request.admin().name(), request.admin().email(),
                request.admin().password(), request.admin().passwordConfirmation()));
        return new RegistrationResponse(result.tenantId(), result.userId(), result.slug(), result.plan(),
                result.subscriptionActive(), result.subscriptionPaidUntil(), result.originalPrice(),
                result.discountPercentage(), result.monthlyPrice(), result.couponCode());
    }

    @PostMapping("/plan-whatsapp-simulation")
    WhatsAppSimulationResponse simulatePlanWhatsApp(@Valid @RequestBody WhatsAppSimulationRequest request) {
        var result = registration.simulatePlanWhatsApp(new RegistrationUseCase.PlanWhatsAppCommand(
                request.tradeName(), request.whatsapp(), request.plan(), request.couponCode()));
        return new WhatsAppSimulationResponse(result.deliveryMode(), result.destination(), result.plan(),
                result.planName(), result.originalPrice(), result.discountPercentage(), result.discountAmount(),
                result.monthlyPrice(), result.couponCode(), result.message(), result.simulatedAt());
    }

    @PostMapping("/coupon-validation")
    CouponValidationResponse validateCoupon(@Valid @RequestBody CouponValidationRequest request) {
        var result = registration.validateCoupon(request.plan(), request.couponCode());
        return new CouponValidationResponse(result.plan(), result.originalPrice(), result.discountPercentage(),
                result.discountAmount(), result.monthlyPrice(), result.couponCode(), result.couponApplied());
    }

    record RegistrationRequest(
            @NotBlank @Size(max = 160) String legalName,
            @NotBlank @Size(max = 160) String tradeName,
            @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 80) String slug,
            @NotBlank
            @Pattern(regexp = "(?:\\d{11}|\\d{14})",
                    message = "Informe um CPF com 11 dígitos ou um CNPJ com 14 dígitos.")
            String document,
            @Size(max = 500) String logoUrl,
            @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String primaryColor,
            @NotNull SubscriptionPlan plan,
            @NotBlank @Pattern(regexp = "\\d{10,13}", message = "Informe um WhatsApp com DDD.") String whatsapp,
            @Pattern(regexp = "[A-Za-z0-9_-]{3,40}", message = "Cupom inválido.") String couponCode,
            @NotNull Boolean simulatedPaymentApproved,
            @AssertTrue(message = "Aceite os Termos de Uso.") Boolean termsAccepted,
            @AssertTrue(message = "Confirme a leitura da Política de Privacidade.") Boolean privacyNoticeAcknowledged,
            @NotBlank @Size(max = 30) String termsVersion,
            @NotBlank @Size(max = 30) String privacyVersion,
            @NotNull @Valid AdminRequest admin
    ) {
    }

    record AdminRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank String password,
            @NotBlank String passwordConfirmation
    ) {
    }

    record WhatsAppSimulationRequest(
            @Size(max = 160) String tradeName,
            @NotBlank @Pattern(regexp = "\\d{10,13}", message = "Informe um WhatsApp com DDD.") String whatsapp,
            @NotNull SubscriptionPlan plan,
            @Pattern(regexp = "[A-Za-z0-9_-]{3,40}", message = "Cupom inválido.") String couponCode
    ) {
    }

    record CouponValidationRequest(
            @NotNull SubscriptionPlan plan,
            @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{3,40}", message = "Cupom inválido.") String couponCode
    ) {
    }

    record RegistrationResponse(UUID tenantId, UUID userId, String slug, SubscriptionPlan plan,
                                boolean subscriptionActive, java.time.Instant subscriptionPaidUntil,
                                java.math.BigDecimal originalPrice, java.math.BigDecimal discountPercentage,
                                java.math.BigDecimal monthlyPrice, String couponCode) {
    }

    record RegistrationConfigurationResponse(boolean subscriptionPaymentSimulationEnabled, boolean couponEnabled,
                                             String termsVersion, String privacyVersion) {
    }

    record WhatsAppSimulationResponse(String deliveryMode, String destination, SubscriptionPlan plan,
                                      String planName, java.math.BigDecimal originalPrice,
                                      java.math.BigDecimal discountPercentage, java.math.BigDecimal discountAmount,
                                      java.math.BigDecimal monthlyPrice, String couponCode, String message,
                                      java.time.Instant simulatedAt) {
    }

    record CouponValidationResponse(SubscriptionPlan plan, java.math.BigDecimal originalPrice,
                                    java.math.BigDecimal discountPercentage, java.math.BigDecimal discountAmount,
                                    java.math.BigDecimal monthlyPrice, String couponCode, boolean couponApplied) {
    }
}
