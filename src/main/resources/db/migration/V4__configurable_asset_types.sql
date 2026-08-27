CREATE TABLE asset_types (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    system_default BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_asset_types_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT uk_asset_types_tenant_name UNIQUE (tenant_id, name)
);

CREATE INDEX ix_asset_types_tenant_name ON asset_types (tenant_id, name);

INSERT INTO asset_types (id, tenant_id, code, name, system_default, active, created_at, updated_at)
SELECT gen_random_uuid(), tenant.id, default_type.code, default_type.name, TRUE, TRUE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM tenants tenant
CROSS JOIN (VALUES
    ('VEHICLE', 'Veículo'),
    ('PHONE', 'Telefone'),
    ('COMPUTER', 'Computador'),
    ('EQUIPMENT', 'Equipamento'),
    ('PROPERTY', 'Imóvel'),
    ('OTHER', 'Outro')
) AS default_type(code, name);

ALTER TABLE assets
    ALTER COLUMN type TYPE VARCHAR(50),
    ADD CONSTRAINT fk_assets_type_tenant
        FOREIGN KEY (tenant_id, type) REFERENCES asset_types(tenant_id, code);
