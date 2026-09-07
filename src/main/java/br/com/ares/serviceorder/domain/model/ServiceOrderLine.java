package br.com.ares.serviceorder.domain.model;

import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public record ServiceOrderLine(UUID serviceId, String description, String notes, BigDecimal quantity,
                               String unit, BigDecimal unitPrice, QuoteCalculationMethod calculationMethod,
                               BigDecimal widthMeters, BigDecimal lengthMeters, BigDecimal heightMeters) {

    public ServiceOrderLine(UUID serviceId, String description, BigDecimal quantity,
                            String unit, BigDecimal unitPrice) {
        this(serviceId, description, null, quantity, unit, unitPrice, QuoteCalculationMethod.QUANTITY,
                null, null, null);
    }

    public ServiceOrderLine(UUID serviceId, String description, BigDecimal quantity,
                            String unit, BigDecimal unitPrice, QuoteCalculationMethod calculationMethod,
                            BigDecimal widthMeters, BigDecimal lengthMeters, BigDecimal heightMeters) {
        this(serviceId, description, null, quantity, unit, unitPrice, calculationMethod,
                widthMeters, lengthMeters, heightMeters);
    }

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
        calculationMethod = calculationMethod == null ? QuoteCalculationMethod.QUANTITY : calculationMethod;
        if (calculationMethod == QuoteCalculationMethod.SQUARE_METER
                || calculationMethod == QuoteCalculationMethod.CUBIC_METER) {
            if (widthMeters == null || widthMeters.signum() <= 0
                    || lengthMeters == null || lengthMeters.signum() <= 0) {
                throw BusinessException.badRequest("quote_line_dimensions_invalid",
                        "Informe largura e comprimento maiores que zero para o cálculo por medida.");
            }
            widthMeters = widthMeters.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
            lengthMeters = lengthMeters.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
            if (calculationMethod == QuoteCalculationMethod.CUBIC_METER) {
                if (heightMeters == null || heightMeters.signum() <= 0) {
                    throw BusinessException.badRequest("quote_line_height_invalid",
                            "Informe uma altura maior que zero para o cálculo por metro cúbico.");
                }
                heightMeters = heightMeters.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
                unit = "M3";
            } else {
                heightMeters = null;
                unit = "M2";
            }
        } else {
            widthMeters = null;
            lengthMeters = null;
            heightMeters = null;
        }
        description = description.trim();
        notes = notes == null || notes.isBlank() ? null : notes.trim();
        quantity = quantity.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
        unit = unit.trim().toUpperCase();
        unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal total() {
        return billableQuantity().multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal billableQuantity() {
        if (calculationMethod == QuoteCalculationMethod.SQUARE_METER
                || calculationMethod == QuoteCalculationMethod.CUBIC_METER) {
            BigDecimal measured = quantity.multiply(widthMeters).multiply(lengthMeters);
            if (calculationMethod == QuoteCalculationMethod.CUBIC_METER) {
                measured = measured.multiply(heightMeters);
            }
            return measured.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
        }
        return quantity;
    }
}
