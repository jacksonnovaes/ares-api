INSERT INTO service_order_statuses (
    id, tenant_id, code, name, system_default, active, display_order, created_at, updated_at
)
SELECT gen_random_uuid(), tenant.id, 'COMPLETED', 'Concluída', TRUE, TRUE, 90,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM tenants tenant
ON CONFLICT (tenant_id, code) DO UPDATE
SET name = EXCLUDED.name,
    system_default = TRUE,
    active = TRUE,
    display_order = EXCLUDED.display_order,
    updated_at = CURRENT_TIMESTAMP;
