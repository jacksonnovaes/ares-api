package br.com.ares.identity.application.service;

import br.com.ares.identity.application.port.in.RegistrationUseCase;
import br.com.ares.identity.application.port.out.PasswordHasher;
import br.com.ares.identity.application.port.out.UserRepository;
import br.com.ares.identity.domain.model.*;
import br.com.ares.identity.domain.service.PasswordPolicy;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import br.com.ares.tenant.application.service.SubscriptionPricingService;
import br.com.ares.tenant.domain.model.SubscriptionPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class RegistrationService implements RegistrationUseCase {

    private final TenantManagementUseCase tenants;
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final AuditLogPort audit;
    private final Clock clock;
    private final boolean subscriptionPaymentSimulationEnabled;
    private final SubscriptionPricingService pricing;
    private final String currentTermsVersion;
    private final String currentPrivacyVersion;

    public RegistrationService(TenantManagementUseCase tenants, UserRepository users,
                               PasswordHasher passwordHasher, PasswordPolicy passwordPolicy,
                               AuditLogPort audit, Clock clock,
                               @Value("${ares.features.subscription-payment-simulation-enabled:false}")
                               boolean subscriptionPaymentSimulationEnabled,
                               SubscriptionPricingService pricing,
                               @Value("${ares.legal.terms-version}") String currentTermsVersion,
                               @Value("${ares.legal.privacy-version}") String currentPrivacyVersion) {
        this.tenants = tenants;
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.audit = audit;
        this.clock = clock;
        this.subscriptionPaymentSimulationEnabled = subscriptionPaymentSimulationEnabled;
        this.pricing = pricing;
        this.currentTermsVersion = currentTermsVersion;
        this.currentPrivacyVersion = currentPrivacyVersion;
    }

    @Override
    @Transactional
    public RegistrationResult register(RegisterTenantAdminCommand command) {
        validateLegalAcceptance(command);
        passwordPolicy.requireConfirmation(command.password(), command.passwordConfirmation());
        passwordPolicy.validate(command.password());
        String email = normalizeEmail(command.adminEmail());
        if (users.existsByEmail(email)) {
            throw BusinessException.conflict("email_exists", "Este e-mail já está cadastrado.");
        }

        Instant now = clock.instant();
        var price = pricing.quote(command.plan(), command.couponCode());
        boolean paymentApproved = subscriptionPaymentSimulationEnabled && command.simulatedPaymentApproved();
        Instant paidUntil = paymentApproved ? now.plus(30, ChronoUnit.DAYS) : null;
        var tenant = tenants.create(new TenantManagementUseCase.CreateTenantCommand(
                command.legalName(), command.tradeName(), command.slug(), command.document(),
                command.logoUrl(), command.primaryColor(), command.plan(), paymentApproved,
                paidUntil, price.finalPrice(), price.couponCode(), price.discountPercentage()));
        Set<Role> roles = Set.of(Role.ADMIN);
        var user = new User(UUID.randomUUID(), tenant.id(), null, command.adminName().trim(), email,
                passwordHasher.hash(command.password()), onlyDigits(command.whatsapp()), "Administrador", UserStatus.ACTIVE,
                roles, RolePermissions.defaultsFor(roles), null, now, now, now);
        user = users.save(user);
        var registrationDetails = new java.util.LinkedHashMap<String, Object>();
        registrationDetails.put("slug", tenant.slug());
        registrationDetails.put("plan", command.plan().name());
        registrationDetails.put("subscriptionActive", paymentApproved);
        registrationDetails.put("paymentSimulationEnabled", subscriptionPaymentSimulationEnabled);
        registrationDetails.put("originalPrice", price.originalPrice());
        registrationDetails.put("monthlyPrice", price.finalPrice());
        registrationDetails.put("discountPercentage", price.discountPercentage());
        if (price.couponCode() != null) registrationDetails.put("couponCode", price.couponCode());
        audit.record(tenant.id(), user.id(), "TENANT_REGISTERED", "TENANT",
                tenant.id().toString(), registrationDetails);
        audit.record(tenant.id(), user.id(), "LEGAL_DOCUMENTS_ACCEPTED", "USER", user.id().toString(),
                Map.of("termsVersion", currentTermsVersion, "privacyVersion", currentPrivacyVersion,
                        "ipAddress", limited(command.acceptanceIpAddress(), 45),
                        "userAgent", limited(command.acceptanceUserAgent(), 500)));
        return new RegistrationResult(tenant.id(), user.id(), tenant.slug(), command.plan(),
                paymentApproved, paidUntil, price.originalPrice(), price.discountPercentage(),
                price.finalPrice(), price.couponCode());
    }

    @Override
    public RegistrationConfiguration registrationConfiguration() {
        return new RegistrationConfiguration(subscriptionPaymentSimulationEnabled, pricing.couponEnabled(),
                currentTermsVersion, currentPrivacyVersion);
    }

    @Override
    public WhatsAppSimulation simulatePlanWhatsApp(PlanWhatsAppCommand command) {
        SubscriptionPlan plan = command.plan();
        var priceQuote = pricing.quote(plan, command.couponCode());
        String destination = onlyDigits(command.whatsapp());
        String company = command.tradeName() == null || command.tradeName().isBlank()
                ? "sua empresa" : command.tradeName().trim();
        String price = "R$ " + priceQuote.finalPrice().toPlainString().replace('.', ',');
        String message = "Olá! Esta é uma simulação da Ares para " + company + ". Você escolheu o plano "
                + plan.displayName() + " por " + price + " ao mês"
                + (priceQuote.couponApplied() ? " com o cupom " + priceQuote.couponCode() : "")
                + ". Após a confirmação da mensalidade, "
                + "o acesso da empresa ficará ativo.";
        return new WhatsAppSimulation("SIMULATION", destination, plan, plan.displayName(),
                priceQuote.originalPrice(), priceQuote.discountPercentage(), priceQuote.discountAmount(),
                priceQuote.finalPrice(), priceQuote.couponCode(), message, clock.instant());
    }

    @Override
    public CouponValidation validateCoupon(SubscriptionPlan plan, String couponCode) {
        var price = pricing.quote(plan, couponCode);
        return new CouponValidation(plan, price.originalPrice(), price.discountPercentage(),
                price.discountAmount(), price.finalPrice(), price.couponCode(), price.couponApplied());
    }

    private void validateLegalAcceptance(RegisterTenantAdminCommand command) {
        if (!command.termsAccepted() || !command.privacyNoticeAcknowledged()) {
            throw BusinessException.badRequest("legal_acceptance_required",
                    "Aceite os Termos de Uso e confirme a leitura da Política de Privacidade.");
        }
        if (!currentTermsVersion.equals(command.termsVersion())
                || !currentPrivacyVersion.equals(command.privacyVersion())) {
            throw BusinessException.badRequest("legal_documents_outdated",
                    "Os documentos legais foram atualizados. Recarregue a página e revise as versões atuais.");
        }
    }

    private String limited(String value, int maxLength) {
        if (value == null || value.isBlank()) return "não informado";
        String normalized = value.trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
