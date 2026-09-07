ALTER TABLE service_orders
    ADD COLUMN delivered_at TIMESTAMPTZ,
    ADD COLUMN delivery_received_by VARCHAR(160),
    ADD COLUMN warranty_days INTEGER,
    ADD COLUMN warranty_until TIMESTAMPTZ,
    ADD COLUMN warranty_terms VARCHAR(2000),
    ADD COLUMN delivery_notes VARCHAR(2000),
    ADD CONSTRAINT ck_orders_warranty_days
        CHECK (warranty_days IS NULL OR warranty_days BETWEEN 0 AND 3650);

-- Ordens já concluídas passam a ter uma entrega básica, preservando o histórico.
UPDATE service_orders
SET delivered_at = completed_at,
    warranty_days = 0
WHERE status = 'COMPLETED'
  AND completed_at IS NOT NULL;
