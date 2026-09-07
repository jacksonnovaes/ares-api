package br.com.ares.serviceorder.adapter.out.document;

import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase.AssetView;
import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase.CompanyView;
import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase.CustomerView;
import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase.DeliveryView;
import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase.OrderView;
import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase.QuoteLineView;
import br.com.ares.serviceorder.application.port.in.ServiceOrderDocumentUseCase.ServiceOrderDocument;
import br.com.ares.serviceorder.domain.model.ServiceOrderPriority;
import br.com.ares.tenant.application.port.in.PublicProfileMediaUseCase;
import br.com.ares.tenant.domain.model.QuoteCalculationMethod;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenHtmlServiceOrderPdfGeneratorTest {

    private static final String TENANT_ID = "11111111-1111-1111-1111-111111111111";
    private static final String LOGO_FILENAME = "brand-22222222-2222-2222-2222-222222222222.png";
    private static final byte[] LOGO = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");

    @Test
    void generatesAReadablePdfWithTheCompleteServiceOrder() throws Exception {
        var media = mock(PublicProfileMediaUseCase.class);
        when(media.load(TENANT_ID, LOGO_FILENAME))
                .thenReturn(new PublicProfileMediaUseCase.MediaContent(LOGO, "image/png"));
        byte[] pdf = new OpenHtmlServiceOrderPdfGenerator(media,
                Clock.fixed(Instant.parse("2026-09-07T15:30:00Z"), ZoneOffset.UTC)).generate(document());

        assertThat(pdf).startsWith("%PDF".getBytes()).hasSizeGreaterThan(1_000);
        try (var loaded = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(loaded);
            assertThat(loaded.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            assertThat(loaded.getPage(0).getResources().getXObjectNames()).isNotEmpty();
            assertThat(text).contains("Oficina Ares", "Revisão preventiva", "Troca de óleo",
                    "R$ 350,00", "Entrega e garantia", "90 dias");
        }
        verify(media).load(TENANT_ID, LOGO_FILENAME);

    }

    @Test
    void omitsTheAssetSectionWhenTheOrderHasNoAsset() throws Exception {
        var media = mock(PublicProfileMediaUseCase.class);
        when(media.load(TENANT_ID, LOGO_FILENAME))
                .thenReturn(new PublicProfileMediaUseCase.MediaContent(LOGO, "image/png"));
        var withAsset = document();
        var withoutAsset = new ServiceOrderDocument(withAsset.order(), withAsset.company(), withAsset.customer(),
                null, withAsset.quoteLines(), withAsset.delivery());

        byte[] pdf = new OpenHtmlServiceOrderPdfGenerator(media,
                Clock.fixed(Instant.parse("2026-09-07T15:30:00Z"), ZoneOffset.UTC)).generate(withoutAsset);

        try (var loaded = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(loaded);
            assertThat(text).doesNotContain("Ativo em manutenção", "Serviço sem ativo", "Tipo de atendimento");
            assertThat(text).contains("CLIENTE", "Maria da Silva", "Revisão preventiva");
        }

        String qaOutput = System.getProperty("pdf.qa.output");
        if (qaOutput != null && !qaOutput.isBlank()) {
            Path path = Path.of(qaOutput);
            Files.createDirectories(path.getParent());
            Files.write(path, pdf);
        }
    }

    private ServiceOrderDocument document() {
        Instant now = Instant.parse("2026-09-07T15:30:00Z");
        UUID orderId = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        var order = new OrderView(orderId, "Revisão preventiva", "Executar revisão completa.", "COMPLETED",
                "Concluída", ServiceOrderPriority.NORMAL, new BigDecimal("350.00"), new BigDecimal("350.00"),
                now, now.plusSeconds(86_400), now, now, now);
        var company = new CompanyView(UUID.fromString(TENANT_ID), "Ares Serviços Ltda.", "Oficina Ares",
                "12345678000190", TENANT_ID + "/" + LOGO_FILENAME, "#2457E6");
        var customer = new CustomerView(UUID.randomUUID(), "Maria da Silva", "12345678901",
                "cliente@example.com", "(11) 99999-9999", "Rua das Flores, 100 - Centro");
        var asset = new AssetView(UUID.randomUUID(), "VEHICLE", "Veículo", "Veículo principal",
                "Toyota", "Corolla", "ABC123");
        var line = new QuoteLineView(UUID.randomUUID(), "Troca de óleo", "Utilizar óleo sintético",
                BigDecimal.ONE, "UN", new BigDecimal("350.00"), QuoteCalculationMethod.QUANTITY,
                null, null, null, BigDecimal.ONE, new BigDecimal("350.00"));
        var delivery = new DeliveryView(now, "Maria da Silva", 90, now.plusSeconds(90L * 86_400),
                "Garantia dos serviços executados.", "Entregue testado.");
        return new ServiceOrderDocument(order, company, customer, asset, List.of(line), delivery);
    }
}
