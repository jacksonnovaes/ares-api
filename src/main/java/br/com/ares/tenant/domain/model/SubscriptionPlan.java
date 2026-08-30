package br.com.ares.tenant.domain.model;

import java.math.BigDecimal;
import java.util.List;

public enum SubscriptionPlan {
    SOLO("Solo", new BigDecimal("29.90"), new BigDecimal("299.00"), 1, List.of(
            "Clientes e catálogo de serviços",
            "Agenda de atendimentos",
            "Orçamentos e ordens de serviço",
            "PDF e histórico de atendimentos"
    )),
    PRO("Pro", new BigDecimal("69.90"), new BigDecimal("699.00"), 3, List.of(
            "Tudo do Solo",
            "Gestão de técnicos e equipe",
            "Personalização e dashboard operacional"
    )),
    BUSINESS("Business", new BigDecimal("149.90"), new BigDecimal("1499.00"), 10, List.of(
            "Tudo do Pro",
            "Permissões por perfil",
            "Gestão avançada e indicadores gerenciais"
    ));

    private final String displayName;
    private final BigDecimal monthlyPrice;
    private final BigDecimal annualPrice;
    private final int includedUsers;
    private final List<String> features;

    SubscriptionPlan(String displayName, BigDecimal monthlyPrice, BigDecimal annualPrice,
                     int includedUsers, List<String> features) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.annualPrice = annualPrice;
        this.includedUsers = includedUsers;
        this.features = List.copyOf(features);
    }

    public String displayName() {
        return displayName;
    }

    public BigDecimal monthlyPrice() {
        return monthlyPrice;
    }

    public BigDecimal annualPrice() {
        return annualPrice;
    }

    public BigDecimal priceFor(SubscriptionBillingCycle billingCycle) {
        return billingCycle == SubscriptionBillingCycle.ANNUAL ? annualPrice : monthlyPrice;
    }

    public int includedUsers() {
        return includedUsers;
    }

    public List<String> features() {
        return features;
    }
}
