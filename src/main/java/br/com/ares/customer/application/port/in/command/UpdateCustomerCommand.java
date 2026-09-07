package br.com.ares.customer.application.port.in.command;

public record UpdateCustomerCommand(String name, String email, String phone, String address, String notes) {
}
