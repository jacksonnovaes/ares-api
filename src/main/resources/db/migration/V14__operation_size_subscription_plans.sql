ALTER TABLE tenants DROP CONSTRAINT ck_tenants_subscription_plan;
ALTER TABLE tenants ALTER COLUMN subscription_plan DROP DEFAULT;

UPDATE tenants
SET subscription_plan = CASE subscription_plan
    WHEN 'ESSENTIAL' THEN 'SOLO'
    WHEN 'PROFESSIONAL' THEN 'PRO'
    ELSE 'BUSINESS'
END;

ALTER TABLE tenants
    ALTER COLUMN subscription_plan SET DEFAULT 'SOLO',
    ADD CONSTRAINT ck_tenants_subscription_plan
        CHECK (subscription_plan IN ('SOLO', 'PRO', 'BUSINESS'));

ALTER TABLE tenants RENAME COLUMN subscription_monthly_price TO subscription_price;
ALTER TABLE tenants RENAME CONSTRAINT ck_tenants_subscription_monthly_price TO ck_tenants_subscription_price;

ALTER TABLE tenants
    ADD COLUMN subscription_billing_cycle VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    ADD COLUMN additional_user_seats INTEGER NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_tenants_subscription_billing_cycle
        CHECK (subscription_billing_cycle IN ('MONTHLY', 'ANNUAL')),
    ADD CONSTRAINT ck_tenants_additional_user_seats
        CHECK (additional_user_seats BETWEEN 0 AND 100);

-- Reprecifica assinaturas existentes na nova tabela, preservando o percentual de cupom registrado.
UPDATE tenants
SET subscription_price = ROUND(
    (CASE subscription_plan
        WHEN 'SOLO' THEN 29.90
        WHEN 'PRO' THEN 69.90
        ELSE 149.90
    END) * (1 - coupon_discount_percentage / 100),
    2
);
