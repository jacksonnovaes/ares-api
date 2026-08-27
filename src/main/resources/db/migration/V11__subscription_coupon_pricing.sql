ALTER TABLE tenants
    ADD COLUMN subscription_monthly_price NUMERIC(15,2),
    ADD COLUMN coupon_code VARCHAR(40),
    ADD COLUMN coupon_discount_percentage NUMERIC(5,2) NOT NULL DEFAULT 0;

UPDATE tenants
SET subscription_monthly_price = CASE subscription_plan
    WHEN 'ESSENTIAL' THEN 49.90
    WHEN 'PROFESSIONAL' THEN 99.90
    WHEN 'BUSINESS' THEN 199.90
END;

ALTER TABLE tenants
    ALTER COLUMN subscription_monthly_price SET NOT NULL,
    ADD CONSTRAINT ck_tenants_subscription_monthly_price CHECK (subscription_monthly_price >= 0),
    ADD CONSTRAINT ck_tenants_coupon_discount CHECK (
        coupon_discount_percentage >= 0 AND coupon_discount_percentage <= 100
    );
