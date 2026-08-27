package br.com.ares.identity.application.port.in;

import br.com.ares.tenant.domain.model.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RegistrationUseCase {
    RegistrationResult register(RegisterTenantAdminCommand command);

    RegistrationConfiguration registrationConfiguration();

    WhatsAppSimulation simulatePlanWhatsApp(PlanWhatsAppCommand command);

    CouponValidation validateCoupon(SubscriptionPlan plan, String couponCode);

    record RegisterTenantAdminCommand(
            String legalName, String tradeName, String slug, String document,
            String logoUrl, String primaryColor, SubscriptionPlan plan, String whatsapp,
            String couponCode, boolean simulatedPaymentApproved,
            boolean termsAccepted, boolean privacyNoticeAcknowledged,
            String termsVersion, String privacyVersion, String acceptanceIpAddress, String acceptanceUserAgent,
            String adminName, String adminEmail, String password, String passwordConfirmation
    ) {
    }

    record RegistrationResult(UUID tenantId, UUID userId, String slug, SubscriptionPlan plan,
                              boolean subscriptionActive, Instant subscriptionPaidUntil,
                              BigDecimal originalPrice, BigDecimal discountPercentage,
                              BigDecimal monthlyPrice, String couponCode) {
    }

    record RegistrationConfiguration(boolean subscriptionPaymentSimulationEnabled, boolean couponEnabled,
                                     String termsVersion, String privacyVersion) {
    }

    record PlanWhatsAppCommand(String tradeName, String whatsapp, SubscriptionPlan plan, String couponCode) {
    }

    record WhatsAppSimulation(String deliveryMode, String destination, SubscriptionPlan plan, String planName,
                              BigDecimal originalPrice, BigDecimal discountPercentage, BigDecimal discountAmount,
                              BigDecimal monthlyPrice, String couponCode, String message, Instant simulatedAt) {
    }

    record CouponValidation(SubscriptionPlan plan, BigDecimal originalPrice, BigDecimal discountPercentage,
                            BigDecimal discountAmount, BigDecimal monthlyPrice, String couponCode,
                            boolean couponApplied) {
    }
}
