package br.com.ares.serviceorder.adapter.out.document;

import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase.ServiceOrderDocument;
import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase.QuoteLineView;
import br.com.ares.serviceorder.application.port.out.ServiceOrderPdfGenerator;
import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.PublicProfileMediaUseCase;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

@Component
class OpenHtmlServiceOrderPdfGenerator implements ServiceOrderPdfGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenHtmlServiceOrderPdfGenerator.class);
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", PT_BR)
            .withZone(SAO_PAULO);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR)
            .withZone(SAO_PAULO);
    private final PublicProfileMediaUseCase media;
    private final Clock clock;

    OpenHtmlServiceOrderPdfGenerator(PublicProfileMediaUseCase media, Clock clock) {
        this.media = media;
        this.clock = clock;
    }

    @Override
    public byte[] generate(ServiceOrderDocument document) {
        try (var output = new ByteArrayOutputStream()) {
            var builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> font("Roboto-Regular.ttf"), "Roboto", 400, FontStyle.NORMAL, true);
            builder.useFont(() -> font("Roboto-Bold.ttf"), "Roboto", 700, FontStyle.NORMAL, true);
            builder.withHtmlContent(html(document), null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "service_order_pdf_generation_failed",
                    "Não foi possível gerar o PDF da ordem de serviço.");
        }
    }

    private String html(ServiceOrderDocument document) {
        var order = document.order();
        var company = document.company();
        var customer = document.customer();
        String color = safeColor(company.primaryColor());
        String documentLabel = document.delivery() == null
                ? "Orçamento / ordem de serviço" : "Termo de entrega e garantia";
        String brandLogo = logo(company.logoUrl());
        String brandCopyClass = brandLogo.isEmpty() ? "brand-copy no-logo" : "brand-copy";

        var rows = new StringBuilder();
        for (QuoteLineView line : document.quoteLines()) {
            rows.append("<tr><td><strong>").append(escape(line.description())).append("</strong>");
            if (hasText(line.notes())) {
                rows.append("<br/><span class='muted'>Observações: ").append(escape(line.notes())).append("</span>");
            }
            rows.append("</td><td class='number'>").append(decimal(line.billableQuantity(), line.quantity()))
                    .append("</td><td>").append(escape(unit(line))).append("</td><td class='number'>")
                    .append(money(line.unitPrice())).append("</td><td class='number strong'>")
                    .append(money(line.total())).append("</td></tr>");
        }
        if (rows.isEmpty()) {
            rows.append("<tr><td colspan='5' class='muted'>Nenhum item informado.</td></tr>");
        }

        StringBuilder delivery = new StringBuilder();
        if (document.delivery() != null) {
            var value = document.delivery();
            delivery.append("<section class='delivery'><h2>Entrega e garantia</h2><div class='grid three'>")
                    .append(detail("Recebido por", fallback(value.receivedBy(), customer.name())))
                    .append(detail("Data da entrega", dateTime(value.deliveredAt())))
                    .append(detail("Garantia", value.warrantyDays() > 0
                            ? value.warrantyDays() + " dias - até " + date(value.warrantyUntil())
                            : "Sem garantia adicional informada"))
                    .append("</div>");
            if (hasText(value.warrantyTerms())) {
                delivery.append(paragraph("Condições da garantia", value.warrantyTerms()));
            }
            if (hasText(value.notes())) {
                delivery.append(paragraph("Observações da entrega", value.notes()));
            }
            delivery.append("</section>");
        }

        String asset = document.asset() == null
                ? ""
                : "<div><span class='label'>Ativo em manutenção</span><strong>" + escape(document.asset().name())
                    + "</strong><span class='muted'>" + escape(joinNonBlank(" - ", document.asset().typeName(),
                    document.asset().brand(), document.asset().model())) + "</span>"
                    + (hasText(document.asset().serialNumber())
                    ? "<span class='muted'>Série: " + escape(document.asset().serialNumber()) + "</span>" : "")
                    + "</div>";

        return """
                <!DOCTYPE html><html xmlns="http://www.w3.org/1999/xhtml" lang="pt-BR"><head><meta charset="UTF-8"/>
                <style>
                @page { size: A4; margin: 18mm 15mm 17mm; @bottom-right { content: "Página " counter(page) " de " counter(pages); font-family: Roboto, sans-serif; font-size: 8pt; color: #667085; } }
                * { box-sizing: border-box; } body { font-family: Roboto, sans-serif; color: #172033; font-size: 10pt; line-height: 1.4; }
                header { border-bottom: 4px solid %s; padding-bottom: 12px; margin-bottom: 18px; }
                .brand { display: inline-block; width: 65%%; vertical-align: top; } .document-id { display: inline-block; width: 34%%; text-align: right; vertical-align: top; }
                .brand-logo { display: inline-block; width: 58px; height: auto; max-height: 58px; margin-right: 11px; vertical-align: middle; }
                .brand-copy { display: inline-block; width: 78%%; vertical-align: middle; } .brand-copy.no-logo { width: 100%%; }
                h1 { margin: 0; font-size: 18pt; } h2 { margin: 0 0 8px; font-size: 13pt; } .id { color: %s; font-size: 17pt; font-weight: bold; }
                .label { display: block; color: #667085; font-size: 8pt; font-weight: bold; text-transform: uppercase; margin-bottom: 3px; }
                .muted { display: block; color: #667085; font-size: 9pt; } .grid { width: 100%%; margin: 12px 0; }
                .grid > div { display: inline-block; width: 49%%; padding-right: 14px; vertical-align: top; }
                .grid.three > div { width: 32.5%%; } .summary { background: #f7f8fa; padding: 11px; margin: 16px 0; }
                .summary > div { display: inline-block; width: 24%%; vertical-align: top; }
                .description { white-space: pre-wrap; color: #475467; margin: 4px 0 14px; }
                table { width: 100%%; border-collapse: collapse; margin-top: 7px; } th, td { border: 1px solid #e5e9f2; padding: 7px; vertical-align: top; }
                th { background: #f7f8fa; text-align: left; font-size: 8pt; } .number { text-align: right; white-space: nowrap; } .strong { font-weight: bold; }
                .totals { margin: 14px 0 0 auto; width: 45%%; } .totals td { border: 0; padding: 3px; } .final { color: %s; font-size: 12pt; font-weight: bold; }
                .delivery { border: 1px solid #d9e2f5; border-left: 5px solid %s; padding: 12px; margin-top: 20px; page-break-inside: avoid; }
                .delivery h2 { color: %s; } .paragraph { margin-top: 10px; white-space: pre-wrap; }
                .signatures { margin-top: 55px; page-break-inside: avoid; } .signature { display: inline-block; width: 43%%; margin: 0 3%%; border-top: 1px solid #98a2b3; padding-top: 5px; text-align: center; color: #667085; font-size: 8pt; }
                </style></head><body>
                <header><div class="brand">%s<div class="%s"><h1>%s</h1><span class="muted">Razão social: %s</span>%s</div></div>
                <div class="document-id"><span class="label">%s</span><div class="id">#%s</div><span class="muted">Emitido em %s</span></div></header>
                <div class="grid"><div><span class="label">Cliente</span><strong>%s</strong><span class="muted">%s</span>%s</div>%s</div>
                <h2>%s</h2><div class="description">%s</div>
                <div class="summary">%s%s%s%s</div>
                <h2>Orçamento</h2><table><thead><tr><th>Descrição</th><th class="number">Qtd./área</th><th>Un.</th><th class="number">Valor unitário</th><th class="number">Total</th></tr></thead><tbody>%s</tbody></table>
                <table class="totals"><tr><td>Valor estimado</td><td class="number strong">%s</td></tr>%s</table>
                %s
                <div class="signatures"><div class="signature">Responsável pelo atendimento</div><div class="signature">%s</div></div>
                </body></html>
                """.formatted(color, color, color, color, color,
                brandLogo, brandCopyClass, escape(company.tradeName()), escape(company.legalName()),
                optionalLine("CPF/CNPJ: ", company.document()), escape(documentLabel), shortId(order.id()),
                dateTime(clock.instant()), escape(customer.name()), escape(joinNonBlank(" - ", customer.email(), customer.phone())),
                optionalLine("CPF/CNPJ: ", customer.document()), asset, escape(order.title()), escape(fallback(order.description(), "Sem descrição adicional.")),
                detail("Status", order.statusName()), detail("Prioridade", priority(order.priority().name())),
                detail("Abertura", dateTime(order.openedAt())), detail(document.delivery() == null ? "Prazo" : "Entrega",
                        dateTime(document.delivery() == null ? order.dueAt() : document.delivery().deliveredAt())),
                rows, money(order.estimatedValue()), order.finalValue() == null ? "" : "<tr><td>Valor final</td><td class='number final'>" + money(order.finalValue()) + "</td></tr>",
                delivery, document.delivery() == null ? "Cliente" : "Recebedor: " + escape(fallback(document.delivery().receivedBy(), customer.name())));
    }

    private String detail(String label, String value) {
        return "<div><span class='label'>" + escape(label) + "</span><strong>" + escape(fallback(value, "Não informado")) + "</strong></div>";
    }

    private String paragraph(String label, String value) {
        return "<div class='paragraph'><span class='label'>" + escape(label) + "</span>" + escape(value) + "</div>";
    }

    private String optionalLine(String prefix, String value) {
        return hasText(value) ? "<span class='muted'>" + escape(prefix + value) + "</span>" : "";
    }

    private String unit(QuoteLineView line) {
        return switch (line.calculationMethod()) {
            case SQUARE_METER -> "m²";
            case CUBIC_METER -> "m³";
            default -> fallback(line.unit(), "UN");
        };
    }

    private String decimal(BigDecimal preferred, BigDecimal fallback) {
        BigDecimal value = preferred == null ? fallback : preferred;
        return value == null ? "-" : value.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private String money(BigDecimal value) {
        return value == null ? "-" : escape(NumberFormat.getCurrencyInstance(PT_BR).format(value));
    }

    private String dateTime(Instant value) {
        return value == null ? "Não informado" : DATE_TIME.format(value);
    }

    private String date(Instant value) {
        return value == null ? "Não informado" : DATE.format(value);
    }

    private String priority(String value) {
        return switch (value) {
            case "LOW" -> "Baixa";
            case "HIGH" -> "Alta";
            case "URGENT" -> "Urgente";
            default -> "Normal";
        };
    }

    private String shortId(java.util.UUID id) {
        return id.toString().substring(0, 8).toUpperCase(PT_BR);
    }

    private java.io.InputStream font(String filename) {
        return OpenHtmlServiceOrderPdfGenerator.class
                .getResourceAsStream("/com/cathive/fonts/roboto/" + filename);
    }

    private String logo(String logoUrl) {
        String[] location = internalMediaLocation(logoUrl);
        if (location == null) return "";
        try {
            var content = media.load(location[0], location[1]);
            if (content.bytes() == null || content.bytes().length == 0 || !hasText(content.contentType())
                    || !content.contentType().matches("image/(?:png|jpeg|webp)")) {
                return "";
            }
            String encoded = Base64.getEncoder().encodeToString(content.bytes());
            return "<img class='brand-logo' alt='Logo' src='data:" + content.contentType()
                    + ";base64," + encoded + "'/>";
        } catch (BusinessException exception) {
            LOGGER.warn("Could not load service-order logo {}: {}", logoUrl, exception.code());
            return "";
        }
    }

    private String[] internalMediaLocation(String logoUrl) {
        if (!hasText(logoUrl)) return null;
        String path = logoUrl.trim();
        int marker = path.indexOf("/public/media/");
        if (marker >= 0) {
            path = path.substring(marker + "/public/media/".length());
        } else if (path.startsWith("/api/backend/public/media/")) {
            path = path.substring("/api/backend/public/media/".length());
        } else if (path.startsWith("/") || path.contains("://")) {
            return null;
        }
        String[] parts = path.split("/", -1);
        if (parts.length != 2 || !parts[0].matches("[0-9a-fA-F-]{36}")
                || !parts[1].matches("brand-[0-9a-fA-F-]{36}\\.(?:png|jpg|webp)")) {
            return null;
        }
        return parts;
    }

    private String safeColor(String value) {
        return value != null && value.matches("#[0-9a-fA-F]{6}") ? value : "#2457E6";
    }

    private String joinNonBlank(String separator, String... values) {
        return java.util.Arrays.stream(values).filter(this::hasText).collect(java.util.stream.Collectors.joining(separator));
    }

    private String fallback(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
