package br.com.ares.serviceorder.application.port.in;

import br.com.ares.serviceorder.domain.model.ServiceOrderPriority;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ServiceOrderDocumentUseCase {

    ServiceOrderDocument getDocument(UUID orderId);

    EmailDeliveryResult sendEmail(UUID orderId, SendEmailCommand command);

    record SendEmailCommand(String recipient) {
    }

    record ServiceOrderDocument(
            OrderView order,
            CompanyView company,
            CustomerView customer,
            AssetView asset,
            List<QuoteLineView> quoteLines
    ) {
    }

    record OrderView(
            UUID id,
            String title,
            String description,
            String status,
            String statusName,
            ServiceOrderPriority priority,
            BigDecimal estimatedValue,
            BigDecimal finalValue,
            Instant openedAt,
            Instant dueAt,
            Instant completedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    record CompanyView(
            UUID id,
            String legalName,
            String tradeName,
            String document,
            String logoUrl,
            String primaryColor
    ) {
    }

    record CustomerView(
            UUID id,
            String name,
            String document,
            String email,
            String phone
    ) {
    }

    record AssetView(
            UUID id,
            String type,
            String typeName,
            String name,
            String brand,
            String model,
            String serialNumber
    ) {
    }

    record QuoteLineView(
            UUID serviceId,
            String description,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            QuoteCalculationMethod calculationMethod,
            BigDecimal widthMeters,
            BigDecimal lengthMeters,
            BigDecimal heightMeters,
            BigDecimal billableQuantity,
            BigDecimal total
    ) {
    }

    record EmailDeliveryResult(
            String deliveryMode,
            String recipient,
            String subject,
            String body,
            Instant processedAt
    ) {
    }
}
