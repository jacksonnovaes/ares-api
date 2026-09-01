ALTER TABLE tenants
    ADD COLUMN public_page_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN public_headline VARCHAR(180),
    ADD COLUMN public_description VARCHAR(1200),
    ADD COLUMN public_whatsapp VARCHAR(13),
    ADD COLUMN public_email VARCHAR(254),
    ADD COLUMN public_city VARCHAR(120),
    ADD COLUMN public_service_area VARCHAR(180),
    ADD COLUMN public_show_prices BOOLEAN NOT NULL DEFAULT FALSE;

