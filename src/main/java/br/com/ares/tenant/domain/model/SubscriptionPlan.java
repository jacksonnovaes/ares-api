package br.com.ares.tenant.domain.model;

import java.math.BigDecimal;

public enum SubscriptionPlan {
    ESSENTIAL("Essencial", new BigDecimal("49.90")),
    PROFESSIONAL("Profissional", new BigDecimal("99.90")),
    BUSINESS("Empresarial", new BigDecimal("199.90"));

    private final String displayName;
    private final BigDecimal monthlyPrice;

    SubscriptionPlan(String displayName, BigDecimal monthlyPrice) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
    }

    public String displayName() {
        return displayName;
    }

    public BigDecimal monthlyPrice() {
        return monthlyPrice;
    }
}
