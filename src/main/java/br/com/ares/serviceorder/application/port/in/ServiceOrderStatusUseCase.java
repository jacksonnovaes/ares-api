package br.com.ares.serviceorder.application.port.in;

import br.com.ares.serviceorder.domain.model.ServiceOrderStatusDefinition;

import java.util.List;

public interface ServiceOrderStatusUseCase {
    ServiceOrderStatusDefinition create(CreateStatusCommand command);
    List<ServiceOrderStatusDefinition> list();

    record CreateStatusCommand(String name) {
    }
}
