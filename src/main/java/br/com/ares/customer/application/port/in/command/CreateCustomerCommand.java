package br.com.ares.customer.application.port.in.command;

import br.com.ares.customer.domain.model.CustomerType;

public record CreateCustomerCommand(CustomerType type, String name, String document, String email,
                                    String phone, String address, String notes) {
}
