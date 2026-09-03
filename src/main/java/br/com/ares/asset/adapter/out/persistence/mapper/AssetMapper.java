package br.com.ares.asset.adapter.out.persistence.mapper;

import br.com.ares.asset.adapter.out.persistence.AssetJpaEntity;
import br.com.ares.asset.domain.model.Asset;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class AssetMapper {

    static ObjectMapper objectMapper = new ObjectMapper();

    public static AssetJpaEntity toEntity(Asset v) {
        var e = new AssetJpaEntity();
        e.setId(v.id());
        e.setTenantId(v.tenantId());
        e.setCustomerId(v.customerId());
        e.setType(v.type());
        e.setName(v.name());
        e.setBrand(v.brand());
        e.setModel(v.model());
        e.setSerialNumber(v.serialNumber());
        try {
            e.setAttributesJson(objectMapper.writeValueAsString(v.attributes()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid asset attributes", ex);
        }
        e.setCreatedAt(v.createdAt());
        e.setUpdatedAt(v.updatedAt());
        return e;
    }

    public static Asset toDomain(AssetJpaEntity e) {
        try {
            Map<String, String> attributes = objectMapper.readValue(e.getAttributesJson(), new TypeReference<>() {
            });
            return new Asset(e.getId(), e.getTenantId(), e.getCustomerId(), e.getType(), e.getName(), e.getBrand(), e.getModel(), e.getSerialNumber(),
                    Map.copyOf(attributes), e.getCreatedAt(), e.getUpdatedAt());
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid stored asset attributes", ex);
        }
    }
}
