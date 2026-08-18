package br.com.ares.tenant.application.service;

import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import br.com.ares.tenant.application.port.out.TenantRepository;
import br.com.ares.tenant.domain.model.Tenant;
import br.com.ares.tenant.domain.model.TenantStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService implements TenantManagementUseCase {

    private final TenantRepository repository;
    private final Clock clock;

    public TenantService(TenantRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Tenant create(CreateTenantCommand command) {
        String slug = normalizeSlug(command.slug());
        if (repository.existsBySlug(slug)) {
            throw BusinessException.conflict("tenant_slug_exists", "Este slug já está em uso.");
        }
        if (repository.existsByDocument(command.document())) {
            throw BusinessException.conflict("tenant_document_exists", "Já existe uma empresa com este documento.");
        }
        Instant now = clock.instant();
        var tenant = new Tenant(UUID.randomUUID(), command.legalName().trim(), command.tradeName().trim(),
                slug, onlyDigits(command.document()), TenantStatus.ACTIVE, command.logoUrl(),
                command.primaryColor(), now, now);
        return repository.save(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public Tenant requiredById(UUID id) {
        return repository.findById(id).orElseThrow(() ->
                BusinessException.notFound("tenant_not_found", "Tenant não encontrado."));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findBySlug(String slug) {
        return repository.findBySlug(normalizeSlug(slug));
    }

    @Override
    @Transactional
    public Tenant changeStatus(UUID id, TenantStatus status) {
        Tenant current = requiredById(id);
        return repository.save(new Tenant(current.id(), current.legalName(), current.tradeName(), current.slug(),
                current.document(), status, current.logoUrl(), current.primaryColor(),
                current.createdAt(), clock.instant()));
    }

    private String normalizeSlug(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
