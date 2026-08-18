package br.com.ares.customer.application.port.in;

import java.util.UUID;

public interface CustomerDirectory {
    boolean exists(UUID tenantId, UUID customerId);
}
