package br.com.ares.tenant.application.service;

import br.com.ares.servicecatalog.application.port.in.ServiceCatalogDirectory;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.PublicProfileUseCase;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.Tenant;
import br.com.ares.tenant.domain.model.PublicProfileManualService;
import br.com.ares.tenant.domain.model.PublicServiceSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PublicProfileService implements PublicProfileUseCase {

    private final TenantRepository tenants;
    private final ServiceCatalogDirectory services;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public PublicProfileService(TenantRepository tenants, ServiceCatalogDirectory services,
                                CurrentActorProvider currentActor, AuditLogPort audit, Clock clock) {
        this.tenants = tenants;
        this.services = services;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileSettings getSettings() {
        return toSettings(requiredCurrentTenant());
    }

    @Override
    @Transactional
    public ProfileSettings update(UpdateProfileCommand command) {
        var actor = currentActor.requiredActor();
        Tenant current = required(actor.tenantId());
        String headline = clean(command.headline());
        String description = clean(command.description());
        String whatsapp = onlyDigits(command.whatsapp());
        String email = lower(command.email());
        String city = clean(command.city());
        String serviceArea = clean(command.serviceArea());
        List<PublicProfileManualService> manualServices = sanitizeManualServices(command.manualServices());

        if (command.enabled() && (headline == null || description == null || whatsapp == null)) {
            throw BusinessException.badRequest("public_profile_incomplete",
                    "Informe título, apresentação e WhatsApp antes de publicar a página.");
        }
        if (whatsapp != null && (whatsapp.length() < 10 || whatsapp.length() > 13)) {
            throw BusinessException.badRequest("public_profile_whatsapp_invalid",
                    "Informe um WhatsApp válido com DDD.");
        }
        if (command.enabled() && command.serviceSource() == PublicServiceSource.MANUAL && manualServices.isEmpty()) {
            throw BusinessException.badRequest("public_profile_manual_services_required",
                    "Adicione ao menos um serviço manual antes de publicar a página.");
        }
        validateColor(command.accentColor(), "cor de destaque");
        validateColor(command.backgroundColor(), "cor de fundo");
        validateColor(command.textColor(), "cor do texto");
        if (command.backgroundOverlayPercentage() < 0 || command.backgroundOverlayPercentage() > 90) {
            throw BusinessException.badRequest("public_profile_overlay_invalid",
                    "A intensidade da imagem de fundo deve ficar entre 0 e 90.");
        }

        Tenant updated = tenants.save(current.withPublicProfile(command.enabled(), headline, description, whatsapp,
                email, city, serviceArea, command.showPrices(), command.serviceSource(), manualServices,
                command.accentColor().toUpperCase(Locale.ROOT), command.backgroundColor().toUpperCase(Locale.ROOT),
                command.textColor().toUpperCase(Locale.ROOT), command.showLogo(),
                command.backgroundOverlayPercentage(), clock.instant()));
        var details = new LinkedHashMap<String, Object>();
        details.put("enabled", updated.publicPageEnabled());
        details.put("showPrices", updated.publicShowPrices());
        details.put("serviceSource", updated.publicServiceSource().name());
        audit.record(actor.tenantId(), actor.userId(), "PUBLIC_PROFILE_UPDATED", "TENANT",
                actor.tenantId().toString(), details);
        return toSettings(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProfile findPublished(String slug) {
        Tenant tenant = tenants.findBySlug(slug == null ? "" : slug.trim().toLowerCase(Locale.ROOT))
                .filter(value -> value.isActive() && value.publicPageEnabled())
                .orElseThrow(() -> BusinessException.notFound("public_profile_not_found",
                        "Página profissional não encontrada."));
        var publishedServices = tenant.publicServiceSource() == PublicServiceSource.MANUAL
                ? tenant.publicManualServices().stream().map(service -> new PublicService(service.name(),
                        service.description(), tenant.publicShowPrices() ? service.basePrice() : null, null)).toList()
                : services.listActive(tenant.id()).stream()
                        .map(service -> new PublicService(service.name(), service.description(),
                                tenant.publicShowPrices() ? service.basePrice() : null, service.estimatedMinutes()))
                        .toList();
        return new PublicProfile(tenant.slug(), tenant.tradeName(), tenant.logoUrl(), tenant.primaryColor(),
                tenant.publicHeadline(), tenant.publicDescription(), tenant.publicWhatsapp(), tenant.publicEmail(),
                tenant.publicCity(), tenant.publicServiceArea(), tenant.publicShowPrices(),
                tenant.publicServiceSource(), tenant.publicAccentColor(), tenant.publicBackgroundColor(),
                tenant.publicTextColor(), tenant.publicLogoPath(), tenant.publicBackgroundImagePath(),
                tenant.publicShowLogo(), tenant.publicBackgroundOverlayPercentage(), publishedServices);
    }

    private Tenant requiredCurrentTenant() {
        return required(currentActor.requiredActor().tenantId());
    }

    private Tenant required(java.util.UUID tenantId) {
        return tenants.findById(tenantId).orElseThrow(() ->
                BusinessException.notFound("tenant_not_found", "Empresa não encontrada."));
    }

    private ProfileSettings toSettings(Tenant tenant) {
        return new ProfileSettings(tenant.publicPageEnabled(), tenant.slug(), tenant.tradeName(), tenant.logoUrl(),
                tenant.primaryColor(), tenant.publicHeadline(), tenant.publicDescription(), tenant.publicWhatsapp(),
                tenant.publicEmail(), tenant.publicCity(), tenant.publicServiceArea(), tenant.publicShowPrices(),
                tenant.publicServiceSource(), tenant.publicManualServices().stream()
                        .map(value -> new ManualService(value.name(), value.description(), value.basePrice())).toList(),
                tenant.publicAccentColor(), tenant.publicBackgroundColor(), tenant.publicTextColor(),
                tenant.publicLogoPath(), tenant.publicBackgroundImagePath(), tenant.publicShowLogo(),
                tenant.publicBackgroundOverlayPercentage());
    }

    private List<PublicProfileManualService> sanitizeManualServices(List<ManualService> values) {
        if (values == null) return List.of();
        return values.stream().map(value -> {
            String name = clean(value.name());
            if (name == null) {
                throw BusinessException.badRequest("public_profile_manual_service_name_required",
                        "Informe o nome de todos os serviços manuais.");
            }
            BigDecimal price = value.basePrice() == null ? null
                    : value.basePrice().setScale(2, RoundingMode.HALF_UP);
            if (price != null && price.signum() < 0) {
                throw BusinessException.badRequest("public_profile_manual_service_price_invalid",
                        "O preço do serviço manual não pode ser negativo.");
            }
            return new PublicProfileManualService(name, clean(value.description()), price);
        }).toList();
    }

    private void validateColor(String value, String label) {
        if (value == null || !value.matches("^#[0-9A-Fa-f]{6}$")) {
            throw BusinessException.badRequest("public_profile_color_invalid", "Informe uma " + label + " válida.");
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String lower(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.toLowerCase(Locale.ROOT);
    }

    private String onlyDigits(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.replaceAll("\\D", "");
    }
}
