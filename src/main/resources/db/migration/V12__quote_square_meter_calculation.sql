ALTER TABLE tenants
    ADD COLUMN quote_calculation_method VARCHAR(30) NOT NULL DEFAULT 'QUANTITY',
    ADD COLUMN default_square_meter_price NUMERIC(15, 2),
    ADD COLUMN default_cubic_meter_price NUMERIC(15, 2);

ALTER TABLE tenants
    ADD CONSTRAINT ck_tenants_quote_calculation_method
        CHECK (quote_calculation_method IN ('QUANTITY', 'SQUARE_METER', 'CUBIC_METER')),
    ADD CONSTRAINT ck_tenants_square_meter_price
        CHECK (default_square_meter_price IS NULL OR default_square_meter_price > 0),
    ADD CONSTRAINT ck_tenants_cubic_meter_price
        CHECK (default_cubic_meter_price IS NULL OR default_cubic_meter_price > 0);

ALTER TABLE service_order_lines
    ADD COLUMN calculation_method VARCHAR(30) NOT NULL DEFAULT 'QUANTITY',
    ADD COLUMN width_meters NUMERIC(12, 3),
    ADD COLUMN length_meters NUMERIC(12, 3),
    ADD COLUMN height_meters NUMERIC(12, 3);

ALTER TABLE service_order_lines
    ADD CONSTRAINT ck_service_order_lines_calculation_method
        CHECK (calculation_method IN ('QUANTITY', 'SQUARE_METER', 'CUBIC_METER')),
    ADD CONSTRAINT ck_service_order_lines_dimensions
        CHECK (
            (calculation_method = 'QUANTITY' AND width_meters IS NULL AND length_meters IS NULL
                AND height_meters IS NULL)
            OR
            (calculation_method = 'SQUARE_METER' AND width_meters > 0 AND length_meters > 0
                AND height_meters IS NULL)
            OR
            (calculation_method = 'CUBIC_METER' AND width_meters > 0 AND length_meters > 0
                AND height_meters > 0)
        );
