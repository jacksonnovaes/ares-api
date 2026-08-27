package br.com.ares.serviceorder.application.port.in;

import br.com.ares.serviceorder.domain.model.*;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public interface ServiceOrderUseCase {
    ServiceOrder create(CreateOrderCommand command);
    ServiceOrder get(UUID id);
    List<ServiceOrder> list();
    ServiceOrder changeStatus(UUID id, ChangeStatusCommand command);
    ServiceOrder updateQuote(UUID id, UpdateQuoteCommand command);
    record CreateOrderCommand(UUID customerId, UUID assetId, List<QuoteLineCommand> quoteLines, String title,
                              String description, ServiceOrderPriority priority,
                              UUID assignedTechnicianId, Instant dueAt) {}
    record ChangeStatusCommand(String status, BigDecimal finalValue) {}
    record UpdateQuoteCommand(UUID assetId, List<QuoteLineCommand> quoteLines) {}
    record QuoteLineCommand(UUID serviceId, String description, BigDecimal quantity,
                            String unit, BigDecimal unitPrice, QuoteCalculationMethod calculationMethod,
                            BigDecimal widthMeters, BigDecimal lengthMeters, BigDecimal heightMeters) {
        public QuoteLineCommand(UUID serviceId, String description, BigDecimal quantity,
                                String unit, BigDecimal unitPrice) {
            this(serviceId, description, quantity, unit, unitPrice,
                    QuoteCalculationMethod.QUANTITY, null, null, null);
        }
    }
}
