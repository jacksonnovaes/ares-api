-- Empresa inicial da instalação. O e-mail fica como contato do perfil público;
-- a página pública permanece desativada até ser configurada pelo administrador.
WITH seeded_tenant AS (
    INSERT INTO tenants (
        id,
        legal_name,
        trade_name,
        slug,
        document,
        status,
        logo_url,
        primary_color,
        secondary_color,
        border_radius,
        dark_mode,
        require_assets,
        subscription_plan,
        subscription_billing_cycle,
        additional_user_seats,
        subscription_active,
        subscription_price,
        coupon_discount_percentage,
        quote_calculation_method,
        public_page_enabled,
        public_email,
        public_show_prices,
        public_service_source,
        public_accent_color,
        public_background_color,
        public_text_color,
        public_show_logo,
        public_background_overlay_percentage,
        created_at,
        updated_at
    )
    SELECT gen_random_uuid(),
           'ARES',
           'ARES',
           'ares-bootstrap',
           '', -- Documento não informado; não inventar CPF/CNPJ.
           'ACTIVE',
           NULL,
           '#2457E6',
           '#16A085',
           12,
           FALSE,
           TRUE,
           'SOLO',
           'MONTHLY',
           0,
           TRUE,
           19.90,
           0.00,
           'QUANTITY',
           FALSE,
           'jackson.novaes@live.com',
           FALSE,
           'CATALOG',
           '#2457E6',
           '#F6F4ED',
           '#142019',
           TRUE,
           18,
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
        SELECT 1
        FROM tenants
        WHERE slug = 'ares-bootstrap'
           OR LOWER(public_email) = LOWER('jackson.novaes@live.com')
    )
    ON CONFLICT DO NOTHING
    RETURNING id
),
seeded_asset_types AS (
    INSERT INTO asset_types (id, tenant_id, code, name, system_default, active, created_at, updated_at)
    SELECT gen_random_uuid(), seeded_tenant.id, default_type.code, default_type.name, TRUE, TRUE,
           CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM seeded_tenant
    CROSS JOIN (VALUES
        ('VEHICLE', 'Veículo'),
        ('PHONE', 'Telefone'),
        ('COMPUTER', 'Computador'),
        ('EQUIPMENT', 'Equipamento'),
        ('PROPERTY', 'Imóvel'),
        ('OTHER', 'Outro')
    ) AS default_type(code, name)
    RETURNING tenant_id
),
seeded_order_statuses AS (
    INSERT INTO service_order_statuses (
        id, tenant_id, code, name, system_default, active, display_order, created_at, updated_at
    )
    SELECT gen_random_uuid(), seeded_tenant.id, default_status.code, default_status.name, TRUE, TRUE,
           default_status.display_order, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM seeded_tenant
    CROSS JOIN (VALUES
        ('OPEN', 'Aberto', 10),
        ('ANALYSIS', 'Em análise', 20),
        ('EXECUTION', 'Execução', 30),
        ('BLOCKED', 'Bloqueada', 40),
        ('COMPLETED', 'Concluída', 90)
    ) AS default_status(code, name, display_order)
    RETURNING tenant_id
),
seeded_quote_calculation_methods AS (
    INSERT INTO tenant_quote_calculation_methods (tenant_id, calculation_method)
    SELECT seeded_tenant.id, method.code
    FROM seeded_tenant
    CROSS JOIN (VALUES ('QUANTITY'), ('SQUARE_METER'), ('CUBIC_METER')) AS method(code)
    RETURNING tenant_id
)
SELECT COUNT(*) FROM seeded_tenant;
