package br.com.ares.serviceorder.domain.model;

import br.com.ares.shared.domain.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderQuoteTest {

    private static final Instant NOW = Instant.parse("2026-08-24T10:00:00Z");

    @Test
    void calculatesEachLineAndReplacesTheOrderEstimatedValue() {
        UUID serviceId = UUID.randomUUID();
        ServiceOrder order = order();
        var lines = List.of(
                new ServiceOrderLine(serviceId, "Troca de óleo", new BigDecimal("1"),
                        "SERVICO", new BigDecimal("250.00")),
                new ServiceOrderLine(null, "Pastilhas de freio", new BigDecimal("2"),
                        "UN", new BigDecimal("165.50"))
        );

        ServiceOrder updated = order.replaceQuote(lines, order.assetId(), NOW.plusSeconds(60));

        assertThat(updated.estimatedValue()).isEqualByComparingTo("581.00");
        assertThat(updated.serviceIds()).containsExactly(serviceId);
        assertThat(updated.quoteLines()).hasSize(2);
        assertThat(updated.quoteLines().get(1).total()).isEqualByComparingTo("331.00");
    }

    @Test
    void rejectsAnEmptyQuote() {
        assertThatThrownBy(() -> order().replaceQuote(List.of(), order().assetId(), NOW))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pelo menos uma linha");
    }

    private ServiceOrder order() {
        return new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                Set.of(), List.of(), "Reparo", null, "OPEN", ServiceOrderPriority.NORMAL,
                BigDecimal.ZERO, null, null, NOW, null, null, NOW, NOW);
    }
}
