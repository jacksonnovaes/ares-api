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
    ServiceOrder updatePlanning(UUID id, UpdatePlanningCommand command);
    List<TechnicianView> listTechnicians();
    List<TimelineEvent> timeline(UUID id);
    record CreateOrderCommand(UUID customerId, UUID assetId, List<QuoteLineCommand> quoteLines, String title,
                              String description, ServiceOrderPriority priority,
                              UUID assignedTechnicianId, Instant dueAt, Instant scheduledStartAt, Instant scheduledEndAt) {}
    record ChangeStatusCommand(String status, BigDecimal finalValue, String deliveryReceivedBy,
                               Integer warrantyDays, String warrantyTerms, String deliveryNotes) {
        public ChangeStatusCommand(String status, BigDecimal finalValue) {
            this(status, finalValue, null, null, null, null);
        }
    }
    record UpdateQuoteCommand(UUID assetId, List<QuoteLineCommand> quoteLines) {}
    record UpdatePlanningCommand(UUID assignedTechnicianId, Instant dueAt, Instant scheduledStartAt,
                                 Instant scheduledEndAt) {}
    record TechnicianView(UUID id, String name) {}
    record TimelineEvent(UUID id, String action, UUID actorId, String actorName, String detailsJson,
                         Instant occurredAt) {}
    record QuoteLineCommand(UUID serviceId, String description, String notes, BigDecimal quantity,
                            String unit, BigDecimal unitPrice, QuoteCalculationMethod calculationMethod,
                            BigDecimal widthMeters, BigDecimal lengthMeters, BigDecimal heightMeters) {
        public QuoteLineCommand(UUID serviceId, String description, BigDecimal quantity,
                                String unit, BigDecimal unitPrice) {
            this(serviceId, description, null, quantity, unit, unitPrice,
                    QuoteCalculationMethod.QUANTITY, null, null, null);
        }

        public QuoteLineCommand(UUID serviceId, String description, BigDecimal quantity,
                                String unit, BigDecimal unitPrice, QuoteCalculationMethod calculationMethod,
                                BigDecimal widthMeters, BigDecimal lengthMeters, BigDecimal heightMeters) {
            this(serviceId, description, null, quantity, unit, unitPrice, calculationMethod,
                    widthMeters, lengthMeters, heightMeters);
        }
    }
}
