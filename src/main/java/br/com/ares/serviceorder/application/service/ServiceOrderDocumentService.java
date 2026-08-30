package br.com.ares.serviceorder.application.service;

import br.com.ares.asset.application.port.in.AssetUseCase;
import br.com.ares.asset.application.port.in.AssetTypeDirectory;
import br.com.ares.customer.application.port.in.CustomerUseCase;
import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase;
import br.com.ares.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.ares.serviceorder.application.port.out.ServiceOrderEmailSender;
import br.com.ares.serviceorder.domain.model.ServiceOrder;
import br.com.ares.serviceorder.domain.model.ServiceOrderPriority;
import br.com.ares.serviceorder.application.port.in.ServiceOrderStatusDirectory;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.TenantManagementUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ServiceOrderDocumentService implements ServiceOrderDocumentUseCase {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
            .ofPattern("dd/MM/yyyy 'às' HH:mm", PT_BR)
            .withZone(ZoneId.of("America/Sao_Paulo"));

    private final ServiceOrderUseCase orders;
    private final CustomerUseCase customers;
    private final AssetUseCase assets;
    private final AssetTypeDirectory assetTypes;
    private final ServiceOrderStatusDirectory statuses;
    private final TenantManagementUseCase tenants;
    private final ServiceOrderEmailSender emailSender;
    private final CurrentActorProvider currentActor;
    private final AuditLogPort audit;
    private final Clock clock;

    public ServiceOrderDocumentService(
            ServiceOrderUseCase orders,
            CustomerUseCase customers,
            AssetUseCase assets,
            AssetTypeDirectory assetTypes,
            ServiceOrderStatusDirectory statuses,
            TenantManagementUseCase tenants,
            ServiceOrderEmailSender emailSender,
            CurrentActorProvider currentActor,
            AuditLogPort audit,
            Clock clock
    ) {
        this.orders = orders;
        this.customers = customers;
        this.assets = assets;
        this.assetTypes = assetTypes;
        this.statuses = statuses;
        this.tenants = tenants;
        this.emailSender = emailSender;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOrderDocument getDocument(UUID orderId) {
        ServiceOrder order = orders.get(orderId);
        var customer = customers.get(order.customerId());
        var company = tenants.requiredById(order.tenantId());
        var status = statuses.required(order.tenantId(), order.status());
        AssetView assetView = null;
        if (order.assetId() != null) {
            var asset = assets.get(order.assetId());
            var assetType = assetTypes.required(order.tenantId(), asset.type());
            assetView = new AssetView(asset.id(), asset.type(), assetType.name(), asset.name(), asset.brand(),
                    asset.model(), asset.serialNumber());
        }
        return new ServiceOrderDocument(
                new OrderView(order.id(), order.title(), order.description(), order.status(), status.name(), order.priority(),
                        order.estimatedValue(), order.finalValue(), order.openedAt(), order.dueAt(),
                        order.completedAt(), order.createdAt(), order.updatedAt()),
                new CompanyView(company.id(), company.legalName(), company.tradeName(), company.document(),
                        company.logoUrl(), company.primaryColor()),
                new CustomerView(customer.id(), customer.name(), customer.document(), customer.email(),
                        customer.phone(), customer.address()),
                assetView,
                order.quoteLines().stream().map(line -> new QuoteLineView(line.serviceId(), line.description(),
                        line.notes(), line.quantity(), line.unit(), line.unitPrice(), line.calculationMethod(),
                        line.widthMeters(), line.lengthMeters(), line.heightMeters(),
                        line.billableQuantity(), line.total())).toList()
        );
    }

    @Override
    @Transactional
    public EmailDeliveryResult sendEmail(UUID orderId, SendEmailCommand command) {
        ServiceOrderDocument document = getDocument(orderId);
        String requestedRecipient = command == null ? null : command.recipient();
        String recipient = normalizeRecipient(requestedRecipient, document.customer().email());
        String subject = "Ordem de serviço #" + shortId(orderId) + " — " + document.company().tradeName();
        String body = emailBody(document);
        Instant processedAt = clock.instant();

        emailSender.send(new ServiceOrderEmailSender.EmailMessage(recipient, subject, body));
        var actor = currentActor.requiredActor();
        audit.record(actor.tenantId(), actor.userId(), "SERVICE_ORDER_EMAIL_PROCESSED", "SERVICE_ORDER",
                orderId.toString(), Map.of("recipient", recipient, "deliveryMode", emailSender.deliveryMode()));

        return new EmailDeliveryResult(emailSender.deliveryMode(), recipient, subject, body, processedAt);
    }

    private String normalizeRecipient(String requested, String customerEmail) {
        String recipient = requested == null || requested.isBlank() ? customerEmail : requested;
        if (recipient == null || recipient.isBlank()) {
            throw BusinessException.badRequest("service_order_email_missing",
                    "Informe um e-mail ou cadastre o e-mail do cliente antes de continuar.");
        }
        return recipient.trim().toLowerCase(PT_BR);
    }

    private String emailBody(ServiceOrderDocument document) {
        var order = document.order();
        StringBuilder body = new StringBuilder()
                .append("Olá, ").append(document.customer().name()).append("!\n\n")
                .append("Segue o resumo da ordem de serviço #").append(shortId(order.id())).append(".\n\n")
                .append("Empresa: ").append(document.company().tradeName()).append('\n')
                .append("Título: ").append(order.title()).append('\n')
                .append("Status: ").append(order.statusName()).append('\n')
                .append("Prioridade: ").append(priorityLabel(order.priority())).append('\n');

        if (document.asset() != null) {
            body.append("Ativo: ").append(document.asset().name()).append('\n');
        }

        if (order.description() != null && !order.description().isBlank()) {
            body.append("Descrição: ").append(order.description().trim()).append('\n');
        }
        if (order.dueAt() != null) {
            body.append("Prazo: ").append(DATE_TIME.format(order.dueAt())).append('\n');
        }

        body.append("\nItens do orçamento:\n");
        document.quoteLines().forEach(line -> {
            body.append("- ").append(line.description()).append(": ");
            if (line.calculationMethod() == br.com.ares.tenant.domain.model.QuoteCalculationMethod.SQUARE_METER) {
                body.append(line.quantity().stripTrailingZeros().toPlainString()).append(" peça(s) × ")
                        .append(line.widthMeters().stripTrailingZeros().toPlainString()).append(" m × ")
                        .append(line.lengthMeters().stripTrailingZeros().toPlainString()).append(" m = ")
                        .append(line.billableQuantity().stripTrailingZeros().toPlainString()).append(" m² × ");
            } else if (line.calculationMethod()
                    == br.com.ares.tenant.domain.model.QuoteCalculationMethod.CUBIC_METER) {
                body.append(line.quantity().stripTrailingZeros().toPlainString()).append(" peça(s) × ")
                        .append(line.widthMeters().stripTrailingZeros().toPlainString()).append(" m × ")
                        .append(line.lengthMeters().stripTrailingZeros().toPlainString()).append(" m × ")
                        .append(line.heightMeters().stripTrailingZeros().toPlainString()).append(" m = ")
                        .append(line.billableQuantity().stripTrailingZeros().toPlainString()).append(" m³ × ");
            } else {
                body.append(line.quantity().stripTrailingZeros().toPlainString()).append(' ')
                        .append(line.unit()).append(" × ");
            }
            body.append(formatMoney(line.unitPrice()))
                    .append(" = ").append(formatMoney(line.total())).append('\n');
            if (line.notes() != null && !line.notes().isBlank()) {
                body.append("  Observações: ").append(line.notes()).append('\n');
            }
        });

        if (order.estimatedValue() != null) {
            body.append("\nValor estimado: ").append(formatMoney(order.estimatedValue())).append('\n');
        }
        if (order.finalValue() != null) {
            body.append("Valor final: ").append(formatMoney(order.finalValue())).append('\n');
        }

        return body.append("\nAtenciosamente,\n").append(document.company().tradeName()).toString();
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(PT_BR);
        return formatter.format(value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP));
    }

    private String shortId(UUID id) {
        return id.toString().substring(0, 8).toUpperCase(PT_BR);
    }

    private String priorityLabel(ServiceOrderPriority priority) {
        return switch (priority) {
            case LOW -> "Baixa";
            case NORMAL -> "Normal";
            case HIGH -> "Alta";
            case URGENT -> "Urgente";
        };
    }
}
