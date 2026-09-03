package br.com.ares.serviceorder.adapter.out.persistence;

import br.com.ares.serviceorder.application.port.out.ServiceOrderRepository;
import br.com.ares.serviceorder.domain.model.ServiceOrder;
import br.com.ares.serviceorder.domain.model.ServiceOrderDelivery;
import br.com.ares.serviceorder.domain.model.ServiceOrderLine;
import br.com.ares.serviceorder.domain.model.ServiceOrderPriority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ServiceOrderQuotePersistenceIntegrationTest {

    @Autowired ServiceOrderRepository repository;

    @Test
    void preservesQuoteLineOrderAndValues() {
        Instant now = Instant.parse("2026-08-24T10:00:00Z");
        UUID orderId = UUID.randomUUID();
        var lines = List.of(
                new ServiceOrderLine(null, "Demolição da parede", "Retirar o entulho ao finalizar",
                        new BigDecimal("8.5"), "M2", new BigDecimal("45.00"),
                        br.com.ares.tenant.domain.model.QuoteCalculationMethod.QUANTITY, null, null, null),
                new ServiceOrderLine(null, "Assentamento de revestimento", new BigDecimal("12"),
                        "M2", new BigDecimal("80.00"))
        );
        var delivery = new ServiceOrderDelivery(now, "Maria da Silva", 90,
                now.plusSeconds(90L * 86400), "Garantia dos serviços executados.", "Entregue testado.");
        var order = new ServiceOrder(orderId, UUID.randomUUID(), UUID.randomUUID(), null, Set.of(),
                lines, "Reforma", "Orçamento de alvenaria", "COMPLETED", ServiceOrderPriority.NORMAL,
                new BigDecimal("1342.50"), null, null, now, null, now, delivery, now, now);

        repository.save(order);
        ServiceOrder restored = repository.findByIdAndTenantId(orderId, order.tenantId()).orElseThrow();

        assertThat(restored.quoteLines()).extracting(ServiceOrderLine::description)
                .containsExactly("Demolição da parede", "Assentamento de revestimento");
        assertThat(restored.quoteLines().getFirst().quantity()).isEqualByComparingTo("8.5");
        assertThat(restored.quoteLines().getFirst().notes()).isEqualTo("Retirar o entulho ao finalizar");
        assertThat(restored.quoteLines().getLast().unitPrice()).isEqualByComparingTo("80.00");
        assertThat(restored.assetId()).isNull();
        assertThat(restored.delivery()).isEqualTo(delivery);
    }
}
