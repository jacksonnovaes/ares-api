CREATE TABLE service_order_statuses (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    system_default BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_order_statuses_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT uk_order_statuses_tenant_name UNIQUE (tenant_id, name),
    CONSTRAINT ck_order_statuses_display_order CHECK (display_order >= 0)
);

CREATE INDEX ix_order_statuses_tenant_order
    ON service_order_statuses (tenant_id, display_order, name);

INSERT INTO service_order_statuses (
    id, tenant_id, code, name, system_default, active, display_order, created_at, updated_at
)
SELECT gen_random_uuid(), tenant.id, value.code, value.name, TRUE, TRUE, value.display_order,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM tenants tenant
CROSS JOIN (VALUES
    ('OPEN', 'Aberto', 10),
    ('ANALYSIS', 'Em análise', 20),
    ('EXECUTION', 'Execução', 30),
    ('BLOCKED', 'Bloqueada', 40)
) AS value(code, name, display_order);

-- Mantém os estados finais já gravados apenas para preservar o histórico das ordens antigas.
INSERT INTO service_order_statuses (
    id, tenant_id, code, name, system_default, active, display_order, created_at, updated_at
)
SELECT gen_random_uuid(), legacy.tenant_id, legacy.code, legacy.name, FALSE, FALSE, legacy.display_order,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT DISTINCT orders.tenant_id, orders.status AS code,
           CASE orders.status WHEN 'COMPLETED' THEN 'Concluída' ELSE 'Cancelada' END AS name,
           CASE orders.status WHEN 'COMPLETED' THEN 90 ELSE 100 END AS display_order
    FROM service_orders orders
    WHERE orders.status IN ('COMPLETED', 'CANCELLED')
) legacy;

ALTER TABLE service_orders DROP CONSTRAINT ck_orders_status;
ALTER TABLE service_orders ALTER COLUMN status TYPE VARCHAR(50);

UPDATE service_orders SET status = 'ANALYSIS' WHERE status = 'IN_DIAGNOSIS';
UPDATE service_orders SET status = 'BLOCKED' WHERE status = 'WAITING_APPROVAL';
UPDATE service_orders SET status = 'EXECUTION' WHERE status = 'IN_PROGRESS';

ALTER TABLE service_orders
    ADD CONSTRAINT fk_orders_status_tenant
        FOREIGN KEY (tenant_id, status) REFERENCES service_order_statuses(tenant_id, code);
