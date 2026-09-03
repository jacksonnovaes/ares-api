ALTER TABLE tenant_public_profile_media
    DROP CONSTRAINT ck_tenant_public_profile_media_kind;

ALTER TABLE tenant_public_profile_media
    ADD CONSTRAINT ck_tenant_public_profile_media_kind
        CHECK (kind IN ('BRAND', 'PROFILE', 'LOGO', 'BACKGROUND'));

