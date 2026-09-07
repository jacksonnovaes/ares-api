CREATE TABLE tenant_communication_settings (
    tenant_id UUID PRIMARY KEY REFERENCES tenants(id) ON DELETE CASCADE,
    whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    whatsapp_number VARCHAR(20),
    whatsapp_token TEXT,
    smtp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    smtp_host VARCHAR(255),
    smtp_port INTEGER,
    smtp_username VARCHAR(254),
    smtp_password TEXT,
    smtp_from_email VARCHAR(254),
    smtp_start_tls BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_tenant_communication_smtp_port
        CHECK (smtp_port IS NULL OR smtp_port BETWEEN 1 AND 65535)
);

