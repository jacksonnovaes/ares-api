ALTER TABLE tenants
    ADD COLUMN public_profile_image_path VARCHAR(500);

CREATE TABLE tenant_public_profile_media (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    kind VARCHAR(20) NOT NULL,
    filename VARCHAR(100) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    content BYTEA NOT NULL,
    size_bytes INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_tenant_public_profile_media_kind UNIQUE (tenant_id, kind),
    CONSTRAINT ck_tenant_public_profile_media_kind
        CHECK (kind IN ('PROFILE', 'LOGO', 'BACKGROUND')),
    CONSTRAINT ck_tenant_public_profile_media_size
        CHECK (size_bytes > 0 AND size_bytes <= 5242880)
);

