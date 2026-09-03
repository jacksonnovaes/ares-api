CREATE TABLE tenant_quote_calculation_methods (
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    calculation_method VARCHAR(30) NOT NULL,
    PRIMARY KEY (tenant_id, calculation_method),
    CONSTRAINT ck_tenant_quote_calculation_method
        CHECK (calculation_method IN ('QUANTITY', 'SQUARE_METER', 'CUBIC_METER'))
);

-- Preserva o comportamento anterior: todos os métodos continuam disponíveis até a empresa personalizar a lista.
INSERT INTO tenant_quote_calculation_methods (tenant_id, calculation_method)
SELECT tenant.id, method.code
FROM tenants tenant
CROSS JOIN (VALUES ('QUANTITY'), ('SQUARE_METER'), ('CUBIC_METER')) AS method(code);
