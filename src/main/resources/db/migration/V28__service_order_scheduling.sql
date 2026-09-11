ALTER TABLE service_orders
    ADD COLUMN scheduled_start_at TIMESTAMPTZ,
    ADD COLUMN scheduled_end_at TIMESTAMPTZ,
    ADD CONSTRAINT ck_orders_schedule_range CHECK (
        (scheduled_start_at IS NULL AND scheduled_end_at IS NULL)
        OR (scheduled_start_at IS NOT NULL AND scheduled_end_at IS NOT NULL
            AND scheduled_end_at > scheduled_start_at)
    );

CREATE INDEX ix_orders_tenant_schedule
    ON service_orders (tenant_id, scheduled_start_at)
    WHERE scheduled_start_at IS NOT NULL;
