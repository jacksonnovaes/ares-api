package br.com.ares.servicecatalog.application.port.in;

import br.com.ares.servicecatalog.domain.model.CatalogService;
import br.com.ares.servicecatalog.domain.model.CatalogServiceType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ServiceCatalogUseCase {
    CatalogService create(CreateServiceCommand command);
    CatalogService get(UUID id);
    List<CatalogService> list();
    record CreateServiceCommand(String name, String description, BigDecimal basePrice,
                                Integer estimatedMinutes, CatalogServiceType type) {}
}
