package br.com.ares.tenant.application.service;

import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.AppearanceSettingsUseCase;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.Tenant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AppearanceSettingsService implements AppearanceSettingsUseCase {

    private final TenantRepository tenants;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public AppearanceSettingsService(TenantRepository tenants, CurrentActorProvider currentActor,
                                     AuditLogPort audit, Clock clock) {
        this.tenants = tenants;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public AppearanceSettings get() {
        return toSettings(required(currentActor.requiredActor().tenantId()));
    }

    @Override
    @Transactional
    public AppearanceSettings update(UpdateAppearanceCommand command) {
        var actor = currentActor.requiredActor();
        String tradeName = command.tradeName() == null ? "" : command.tradeName().trim();
        if (tradeName.length() < 2 || tradeName.length() > 160) {
            throw BusinessException.badRequest("appearance_trade_name_invalid",
                    "Informe um nome da marca entre 2 e 160 caracteres.");
        }
        String primaryColor = normalizedColor(command.primaryColor(), "cor principal");
        String secondaryColor = normalizedColor(command.secondaryColor(), "cor de destaque");
        if (command.borderRadius() < 6 || command.borderRadius() > 24) {
            throw BusinessException.badRequest("appearance_border_radius_invalid",
                    "O arredondamento deve ficar entre 6 e 24 pixels.");
        }
        Tenant updated = tenants.save(required(actor.tenantId()).withAppearance(tradeName, primaryColor,
                secondaryColor, command.borderRadius(), clock.instant()));
        audit.record(actor.tenantId(), actor.userId(), "TENANT_APPEARANCE_UPDATED", "TENANT",
                actor.tenantId().toString(), Map.of("primaryColor", primaryColor,
                        "secondaryColor", secondaryColor, "borderRadius", command.borderRadius()));
        return toSettings(updated);
    }

    private Tenant required(UUID tenantId) {
        return tenants.findById(tenantId).orElseThrow(() ->
                BusinessException.notFound("tenant_not_found", "Empresa não encontrada."));
    }

    private AppearanceSettings toSettings(Tenant tenant) {
        return new AppearanceSettings(tenant.tradeName(), tenant.logoUrl(),
                tenant.primaryColor() == null ? "#2457E6" : tenant.primaryColor(),
                tenant.secondaryColor(), tenant.borderRadius());
    }

    private String normalizedColor(String value, String label) {
        if (value == null || !value.matches("^#[0-9A-Fa-f]{6}$")) {
            throw BusinessException.badRequest("appearance_color_invalid", "Informe uma " + label + " válida.");
        }
        return value.toUpperCase(Locale.ROOT);
    }
}
