ALTER TABLE tenants
    ADD COLUMN secondary_color VARCHAR(7) NOT NULL DEFAULT '#16A085',
    ADD COLUMN border_radius INTEGER NOT NULL DEFAULT 12;

ALTER TABLE tenants
    ADD CONSTRAINT ck_tenants_secondary_color
        CHECK (secondary_color ~ '^#[0-9A-Fa-f]{6}$'),
    ADD CONSTRAINT ck_tenants_border_radius
        CHECK (border_radius BETWEEN 6 AND 24);
