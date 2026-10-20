-- Move the globally unique bootstrap identity to the ARES tenant and rotate its initial password.
-- The previous tenant's refresh and password-reset tokens are invalidated during the transfer.
DO $$
DECLARE
    bootstrap_tenant_id UUID;
    admin_user_id UUID;
BEGIN
    SELECT id
    INTO bootstrap_tenant_id
    FROM tenants
    WHERE slug = 'ares-bootstrap';

    IF bootstrap_tenant_id IS NULL THEN
        RAISE EXCEPTION 'ARES bootstrap tenant was not found';
    END IF;

    SELECT id
    INTO admin_user_id
    FROM users
    WHERE LOWER(email) = LOWER('jackson.novaes@live.com');

    IF admin_user_id IS NULL THEN
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
        ) VALUES (
            gen_random_uuid(),
            bootstrap_tenant_id,
            'Jackson Novaes',
            'jackson.novaes@live.com',
            '$2b$12$yzTFTNI5T0wtXyp16vM3aOsO8cL1.NfWVXWf/XVVuxIkAB2Nt6wnq',
            NULL,
            'Administrador',
            'ACTIVE',
            NULL,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
        RETURNING id INTO admin_user_id;
    ELSE
        IF EXISTS (
            SELECT 1
            FROM service_orders
            WHERE assigned_technician_id = admin_user_id
        ) THEN
            RAISE EXCEPTION 'Cannot transfer ARES admin while service orders are assigned to this user';
        END IF;

        DELETE FROM refresh_tokens WHERE user_id = admin_user_id;
        DELETE FROM password_reset_tokens WHERE user_id = admin_user_id;

        UPDATE users
        SET tenant_id = bootstrap_tenant_id,
            password_hash = '$2b$12$yzTFTNI5T0wtXyp16vM3aOsO8cL1.NfWVXWf/XVVuxIkAB2Nt6wnq',
            password_changed_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = admin_user_id;
    END IF;

    INSERT INTO user_roles (user_id, role)
    VALUES (admin_user_id, 'ADMIN')
    ON CONFLICT DO NOTHING;

    INSERT INTO user_permissions (user_id, permission)
    SELECT admin_user_id, permission.code
    FROM (VALUES
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
    ON CONFLICT DO NOTHING;
END $$;
