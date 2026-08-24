package br.com.ares.serviceorder.adapter.out.persistence;

import br.com.ares.serviceorder.application.port.out.ServiceOrderStatusRepository;
import br.com.ares.serviceorder.domain.model.ServiceOrderStatusDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class ServiceOrderStatusPersistenceAdapter implements ServiceOrderStatusRepository {

    private final SpringDataServiceOrderStatusRepository repository;

    ServiceOrderStatusPersistenceAdapter(SpringDataServiceOrderStatusRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceOrderStatusDefinition save(ServiceOrderStatusDefinition value) {
        return toDomain(repository.save(toEntity(value)));
    }

    @Override
    public Optional<ServiceOrderStatusDefinition> findByTenantIdAndCode(UUID tenantId, String code) {
        return repository.findByTenantIdAndCode(tenantId, code).map(this::toDomain);
    }

    @Override
    public List<ServiceOrderStatusDefinition> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantIdOrderByDisplayOrderAscNameAsc(tenantId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name) {
        return repository.existsByTenantIdAndNameIgnoreCase(tenantId, name);
    }

    private ServiceOrderStatusJpaEntity toEntity(ServiceOrderStatusDefinition value) {
        var entity = new ServiceOrderStatusJpaEntity();
        entity.id = value.id();
        entity.tenantId = value.tenantId();
        entity.code = value.code();
        entity.name = value.name();
        entity.systemDefault = value.systemDefault();
        entity.active = value.active();
        entity.displayOrder = value.displayOrder();
        entity.createdAt = value.createdAt();
        entity.updatedAt = value.updatedAt();
        return entity;
    }

    private ServiceOrderStatusDefinition toDomain(ServiceOrderStatusJpaEntity entity) {
        return new ServiceOrderStatusDefinition(entity.id, entity.tenantId, entity.code, entity.name,
                entity.systemDefault, entity.active, entity.displayOrder, entity.createdAt, entity.updatedAt);
    }
}
