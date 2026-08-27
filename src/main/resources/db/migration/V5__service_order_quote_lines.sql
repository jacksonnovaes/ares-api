CREATE TABLE service_order_lines (
    service_order_id UUID NOT NULL REFERENCES service_orders(id) ON DELETE CASCADE,
    line_position INTEGER NOT NULL,
    service_id UUID REFERENCES catalog_services(id),
    description VARCHAR(500) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    unit_price NUMERIC(15,2) NOT NULL,
    PRIMARY KEY (service_order_id, line_position),
    CONSTRAINT ck_service_order_lines_position CHECK (line_position >= 0),
    CONSTRAINT ck_service_order_lines_quantity CHECK (quantity > 0),
    CONSTRAINT ck_service_order_lines_price CHECK (unit_price >= 0)
);

CREATE INDEX ix_service_order_lines_service ON service_order_lines (service_id);

-- Converte os serviços das ordens existentes em linhas unitárias, preservando o preço-base atual.
INSERT INTO service_order_lines (
    service_order_id, line_position, service_id, description, quantity, unit, unit_price
)
SELECT order_service.service_order_id,
       CAST(ROW_NUMBER() OVER (
           PARTITION BY order_service.service_order_id ORDER BY catalog.name, catalog.id
       ) - 1 AS INTEGER),
       catalog.id,
       catalog.name,
       1.000,
       'UN',
       catalog.base_price
FROM service_order_services order_service
JOIN catalog_services catalog ON catalog.id = order_service.service_id;
