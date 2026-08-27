ALTER TABLE tenants
    ADD COLUMN subscription_plan VARCHAR(30) NOT NULL DEFAULT 'ESSENTIAL',
    ADD COLUMN subscription_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN subscription_paid_until TIMESTAMPTZ;

ALTER TABLE tenants
    ADD CONSTRAINT ck_tenants_subscription_plan
        CHECK (subscription_plan IN ('ESSENTIAL', 'PROFESSIONAL', 'BUSINESS'));
