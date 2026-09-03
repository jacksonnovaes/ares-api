ALTER TABLE tenants
    ADD COLUMN public_service_source VARCHAR(20) NOT NULL DEFAULT 'CATALOG',
    ADD COLUMN public_accent_color VARCHAR(7) NOT NULL DEFAULT '#2457E6',
    ADD COLUMN public_background_color VARCHAR(7) NOT NULL DEFAULT '#F6F4ED',
    ADD COLUMN public_text_color VARCHAR(7) NOT NULL DEFAULT '#142019',
    ADD COLUMN public_logo_path VARCHAR(500),
    ADD COLUMN public_background_image_path VARCHAR(500),
    ADD COLUMN public_show_logo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN public_background_overlay_percentage SMALLINT NOT NULL DEFAULT 18,
    ADD CONSTRAINT ck_tenants_public_service_source
        CHECK (public_service_source IN ('CATALOG', 'MANUAL')),
    ADD CONSTRAINT ck_tenants_public_accent_color
        CHECK (public_accent_color ~ '^#[0-9A-Fa-f]{6}$'),
    ADD CONSTRAINT ck_tenants_public_background_color
        CHECK (public_background_color ~ '^#[0-9A-Fa-f]{6}$'),
    ADD CONSTRAINT ck_tenants_public_text_color
        CHECK (public_text_color ~ '^#[0-9A-Fa-f]{6}$'),
    ADD CONSTRAINT ck_tenants_public_background_overlay
        CHECK (public_background_overlay_percentage BETWEEN 0 AND 90);

UPDATE tenants
SET public_accent_color = primary_color
WHERE primary_color IS NOT NULL;

CREATE TABLE tenant_public_profile_services (
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(1000),
    base_price NUMERIC(15, 2),
    PRIMARY KEY (tenant_id, display_order),
    CONSTRAINT ck_public_profile_service_price CHECK (base_price IS NULL OR base_price >= 0)
);

