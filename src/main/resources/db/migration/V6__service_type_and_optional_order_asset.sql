ALTER TABLE catalog_services
    ADD COLUMN service_type VARCHAR(30) NOT NULL DEFAULT 'MAINTENANCE',
    ADD CONSTRAINT ck_catalog_services_type CHECK (service_type IN ('GENERAL', 'MAINTENANCE'));

ALTER TABLE service_orders
    ALTER COLUMN asset_id DROP NOT NULL;
