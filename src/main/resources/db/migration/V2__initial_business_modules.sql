CREATE TABLE customers (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    type VARCHAR(20) NOT NULL,
    name VARCHAR(160) NOT NULL,
    document VARCHAR(20),
    email VARCHAR(254),
    phone VARCHAR(30),
    notes TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_customers_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT uk_customers_tenant_document UNIQUE (tenant_id, document),
    CONSTRAINT ck_customers_type CHECK (type IN ('PERSON', 'COMPANY')),
    CONSTRAINT ck_customers_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX ix_customers_tenant_name ON customers (tenant_id, name);

ALTER TABLE users
    ADD CONSTRAINT fk_users_customer_tenant
    FOREIGN KEY (customer_id, tenant_id) REFERENCES customers(id, tenant_id);

CREATE TABLE assets (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    customer_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    name VARCHAR(160) NOT NULL,
    brand VARCHAR(100),
    model VARCHAR(100),
    serial_number VARCHAR(120),
    attributes_json TEXT NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_assets_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT uk_assets_id_customer_tenant UNIQUE (id, customer_id, tenant_id),
    CONSTRAINT fk_assets_customer_tenant FOREIGN KEY (customer_id, tenant_id)
        REFERENCES customers(id, tenant_id)
);

CREATE INDEX ix_assets_tenant_customer ON assets (tenant_id, customer_id);

CREATE TABLE catalog_services (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(160) NOT NULL,
    description TEXT,
    base_price NUMERIC(15,2) NOT NULL,
    estimated_minutes INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_catalog_services_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT ck_catalog_services_price CHECK (base_price >= 0),
    CONSTRAINT ck_catalog_services_minutes CHECK (estimated_minutes IS NULL OR estimated_minutes > 0)
);

CREATE INDEX ix_catalog_services_tenant_name ON catalog_services (tenant_id, name);

CREATE TABLE service_orders (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    customer_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    estimated_value NUMERIC(15,2),
    final_value NUMERIC(15,2),
    assigned_technician_id UUID,
    opened_at TIMESTAMPTZ NOT NULL,
    due_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_service_orders_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT fk_orders_customer_tenant FOREIGN KEY (customer_id, tenant_id)
        REFERENCES customers(id, tenant_id),
    CONSTRAINT fk_orders_asset_customer_tenant FOREIGN KEY (asset_id, customer_id, tenant_id)
        REFERENCES assets(id, customer_id, tenant_id),
    CONSTRAINT fk_orders_technician_tenant FOREIGN KEY (assigned_technician_id, tenant_id)
        REFERENCES users(id, tenant_id),
    CONSTRAINT ck_orders_status CHECK (status IN ('OPEN', 'IN_DIAGNOSIS', 'WAITING_APPROVAL',
        'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_orders_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    CONSTRAINT ck_orders_values CHECK ((estimated_value IS NULL OR estimated_value >= 0)
        AND (final_value IS NULL OR final_value >= 0))
);

CREATE INDEX ix_orders_tenant_created ON service_orders (tenant_id, created_at DESC);
CREATE INDEX ix_orders_tenant_customer ON service_orders (tenant_id, customer_id);
CREATE INDEX ix_orders_tenant_technician ON service_orders (tenant_id, assigned_technician_id);

CREATE TABLE service_order_services (
    service_order_id UUID NOT NULL REFERENCES service_orders(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES catalog_services(id),
    PRIMARY KEY (service_order_id, service_id)
);
