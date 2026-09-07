package br.com.ares.customer.adapter.out.mapper;

import br.com.ares.customer.adapter.out.persistence.CustomerJpaEntity;
import br.com.ares.customer.domain.model.Customer;

public class CustomerPersistenceMapper {

    public static CustomerJpaEntity toEntity(Customer value) {
        var entity = new CustomerJpaEntity();
        entity.setId(value.id());
        entity.setTenantId(value.tenantId());
        entity.setType(value.type());
        entity.setName(value.name());
        entity.setDocument(value.document());
        entity.setEmail(value.email());
        entity.setPhone(value.phone());
        entity.setAddress(value.address());
        entity.setNotes(value.notes());
        entity.setStatus(value.status());
        entity.setCreatedAt(value.createdAt());
        entity.setUpdatedAt(value.updatedAt());
        return entity;
    }

    public static Customer toDomain(CustomerJpaEntity value) {
        return new Customer(value.getId(), value.getTenantId(), value.getType(), value.getName(), value.getDocument(), value.getEmail(),
                value.getPhone(), value.getAddress(), value.getNotes(), value.getStatus(), value.getCreatedAt(), value.getUpdatedAt());
    }
}
