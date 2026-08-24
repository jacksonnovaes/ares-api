package br.com.ares.serviceorder.domain.model;

import br.com.ares.shared.domain.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public record ServiceOrderLine(UUID serviceId, String description, BigDecimal quantity,
                               String unit, BigDecimal unitPrice) {

    public ServiceOrderLine {
        if (description == null || description.isBlank()) {
            throw BusinessException.badRequest("quote_line_description_required",
                    "Informe a descrição de todas as linhas do orçamento.");
        }
        if (quantity == null || quantity.signum() <= 0) {
            throw BusinessException.badRequest("quote_line_quantity_invalid",
                    "A quantidade de cada linha deve ser maior que zero.");
        }
        if (unit == null || unit.isBlank()) {
            throw BusinessException.badRequest("quote_line_unit_required",
                    "Informe a unidade de todas as linhas do orçamento.");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw BusinessException.badRequest("quote_line_price_invalid",
                    "O valor unitário não pode ser negativo.");
        }
        description = description.trim();
        quantity = quantity.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
        unit = unit.trim().toUpperCase();
        unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal total() {
        return quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }
}
