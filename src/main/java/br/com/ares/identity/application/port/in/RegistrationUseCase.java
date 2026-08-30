package br.com.ares.identity.application.port.in;

import br.com.ares.tenant.domain.model.SubscriptionBillingCycle;
import br.com.ares.tenant.domain.model.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RegistrationUseCase {
    RegistrationResult register(RegisterTenantAdminCommand command);

    RegistrationConfiguration registrationConfiguration();

    WhatsAppSimulation simulatePlanWhatsApp(PlanWhatsAppCommand command);

    CouponValidation validateCoupon(SubscriptionPlan plan, SubscriptionBillingCycle billingCycle,
                                    int additionalUserSeats, String couponCode);

    record RegisterTenantAdminCommand(
            String legalName, String tradeName, String slug, String document,
            String logoUrl, String primaryColor, SubscriptionPlan plan,
            SubscriptionBillingCycle billingCycle, int additionalUserSeats, String whatsapp,
            String couponCode, boolean simulatedPaymentApproved,
            boolean termsAccepted, boolean privacyNoticeAcknowledged,
            String termsVersion, String privacyVersion, String acceptanceIpAddress, String acceptanceUserAgent,
            String adminName, String adminEmail, String password, String passwordConfirmation
    ) {
    }

    record RegistrationResult(UUID tenantId, UUID userId, String slug, SubscriptionPlan plan,
                              SubscriptionBillingCycle billingCycle, int additionalUserSeats, int userLimit,
                              boolean subscriptionActive, Instant subscriptionPaidUntil,
                              BigDecimal originalPrice, BigDecimal discountPercentage,
                              BigDecimal price, BigDecimal monthlyEquivalent, String couponCode) {
    }

    record RegistrationConfiguration(boolean subscriptionPaymentSimulationEnabled, boolean couponEnabled,
                                     String termsVersion, String privacyVersion, List<PlanOption> plans,
                                     BigDecimal additionalUserMonthlyPrice,
                                     BigDecimal additionalUserAnnualPrice) {
    }

    record PlanOption(SubscriptionPlan code, String name, BigDecimal monthlyPrice, BigDecimal annualPrice,
                      int includedUsers, List<String> features) {
    }

    record PlanWhatsAppCommand(String tradeName, String whatsapp, SubscriptionPlan plan,
                               SubscriptionBillingCycle billingCycle, int additionalUserSeats,
                               String couponCode) {
    }

    record WhatsAppSimulation(String deliveryMode, String destination, SubscriptionPlan plan, String planName,
                              SubscriptionBillingCycle billingCycle, int additionalUserSeats, int userLimit,
                              BigDecimal originalPrice, BigDecimal discountPercentage, BigDecimal discountAmount,
                              BigDecimal price, BigDecimal monthlyEquivalent, String couponCode,
                              String message, Instant simulatedAt) {
    }

    record CouponValidation(SubscriptionPlan plan, SubscriptionBillingCycle billingCycle,
                            int additionalUserSeats, int userLimit, BigDecimal originalPrice,
                            BigDecimal discountPercentage, BigDecimal discountAmount, BigDecimal price,
                            BigDecimal monthlyEquivalent, String couponCode, boolean couponApplied) {
    }
}
