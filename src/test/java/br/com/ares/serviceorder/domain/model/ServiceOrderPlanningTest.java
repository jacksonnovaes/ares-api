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

class ServiceOrderPlanningTest {
    private static final Instant NOW = Instant.parse("2026-09-11T12:00:00Z");

    @Test
    void updatesTechnicianDeadlineAndSchedule() {
        UUID technicianId = UUID.randomUUID();
        Instant start = NOW.plusSeconds(3600);
        Instant end = start.plusSeconds(7200);

        ServiceOrder planned = order().updatePlanning(technicianId, end.plusSeconds(86400), start, end, NOW);

        assertThat(planned.assignedTechnicianId()).isEqualTo(technicianId);
        assertThat(planned.scheduledStartAt()).isEqualTo(start);
        assertThat(planned.scheduledEndAt()).isEqualTo(end);
        assertThat(planned.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsIncompleteOrInvertedSchedule() {
        assertThatThrownBy(() -> order().updatePlanning(null, null, NOW, null, NOW))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> order().updatePlanning(null, null, NOW, NOW.minusSeconds(1), NOW))
                .isInstanceOf(BusinessException.class);
    }

    private ServiceOrder order() {
        return new ServiceOrder(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, Set.of(), List.of(),
                "Atendimento", null, "OPEN", ServiceOrderPriority.NORMAL, BigDecimal.ZERO, null, null, NOW,
                null, null, null, null, null, NOW, NOW);
    }
}
