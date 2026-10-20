-- Cria a conta administradora inicial para a empresa criada em V29.
-- A senha inicial é armazenada somente como BCrypt; troque-a após o primeiro acesso.
INSERT INTO users (
    id,
    tenant_id,
    name,
    email,
    password_hash,
    phone,
    job_title,
    status,
    last_login_at,
    password_changed_at,
    created_at,
    updated_at
)
SELECT gen_random_uuid(),
       tenant.id,
       'Jackson Novaes',
       'jackson.novaes@live.com',
       '$2b$12$UeMyxxti5segjA0Z8H2F0OaKvZx0LHlgGdS.aReDQ8AD6NnBPSPby',
       NULL,
       'Administrador',
       'ACTIVE',
       NULL,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM tenants tenant
WHERE tenant.slug = 'ares-bootstrap'
  AND NOT EXISTS (
      SELECT 1 FROM users existing_user
      WHERE LOWER(existing_user.email) = LOWER('jackson.novaes@live.com')
  )
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT app_user.id, 'ADMIN'
FROM users app_user
JOIN tenants tenant ON tenant.id = app_user.tenant_id
WHERE tenant.slug = 'ares-bootstrap'
  AND LOWER(app_user.email) = LOWER('jackson.novaes@live.com')
ON CONFLICT DO NOTHING;

INSERT INTO user_permissions (user_id, permission)
SELECT app_user.id, permission.code
FROM users app_user
JOIN tenants tenant ON tenant.id = app_user.tenant_id
CROSS JOIN (VALUES
    ('CUSTOMER_READ'),
    ('CUSTOMER_CREATE'),
    ('CUSTOMER_UPDATE'),
    ('CUSTOMER_DELETE'),
    ('ASSET_READ'),
    ('ASSET_CREATE'),
    ('ASSET_UPDATE'),
    ('ASSET_DELETE'),
    ('SERVICE_READ'),
    ('SERVICE_CREATE'),
    ('SERVICE_UPDATE'),
    ('SERVICE_DELETE'),
    ('SERVICE_ORDER_READ'),
    ('SERVICE_ORDER_CREATE'),
    ('SERVICE_ORDER_UPDATE'),
    ('SERVICE_ORDER_CANCEL'),
    ('PAYMENT_READ'),
    ('PAYMENT_CREATE'),
    ('REPORT_READ'),
    ('USER_MANAGE'),
    ('TENANT_CONFIGURE')
) AS permission(code)
WHERE tenant.slug = 'ares-bootstrap'
  AND LOWER(app_user.email) = LOWER('jackson.novaes@live.com')
ON CONFLICT DO NOTHING;
