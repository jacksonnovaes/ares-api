CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    legal_name VARCHAR(160) NOT NULL,
    trade_name VARCHAR(160) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    document VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    logo_url VARCHAR(500),
    primary_color VARCHAR(7),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_tenants_slug UNIQUE (slug),
    CONSTRAINT uk_tenants_document UNIQUE (document),
    CONSTRAINT ck_tenants_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'INACTIVE')),
    CONSTRAINT ck_tenants_primary_color CHECK (primary_color IS NULL OR primary_color ~ '^#[0-9A-Fa-f]{6}$')
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    customer_id UUID,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    job_title VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    last_login_at TIMESTAMPTZ,
    password_changed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_users_status CHECK (status IN ('PENDING', 'ACTIVE', 'BLOCKED', 'INACTIVE')),
    CONSTRAINT uk_users_id_tenant UNIQUE (id, tenant_id)
);

-- O login recebe somente e-mail e senha; por isso a identidade é globalmente única.
CREATE UNIQUE INDEX uk_users_email_lower ON users (LOWER(email));
CREATE INDEX ix_users_tenant ON users (tenant_id);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(40) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE user_permissions (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    permission VARCHAR(80) NOT NULL,
    PRIMARY KEY (user_id, permission)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by UUID,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_refresh_user_tenant FOREIGN KEY (user_id, tenant_id) REFERENCES users(id, tenant_id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_refresh_family ON refresh_tokens (family_id);
CREATE INDEX ix_refresh_user_active ON refresh_tokens (user_id) WHERE revoked_at IS NULL;

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_reset_user_tenant FOREIGN KEY (user_id, tenant_id) REFERENCES users(id, tenant_id),
    CONSTRAINT uk_reset_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_password_reset_user ON password_reset_tokens (user_id);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    tenant_id UUID REFERENCES tenants(id),
    actor_id UUID,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(100),
    details_json TEXT NOT NULL DEFAULT '{}',
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_audit_tenant_occurred ON audit_events (tenant_id, occurred_at DESC);
CREATE INDEX ix_audit_actor ON audit_events (actor_id);
