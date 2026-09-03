package br.com.ares.customer.application.service;

import br.com.ares.customer.application.port.in.CustomerUseCase;
import br.com.ares.customer.application.port.out.CustomerRepository;
import br.com.ares.customer.domain.model.Customer;
import br.com.ares.customer.domain.model.CustomerType;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceAddressTest {

    @Mock CustomerRepository repository;
    @Mock CurrentActorProvider currentActor;
    @Mock AuditLogPort audit;

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService(repository, currentActor, audit,
                Clock.fixed(Instant.parse("2026-08-31T12:00:00Z"), ZoneOffset.UTC));
        when(currentActor.requiredActor()).thenReturn(new AuthenticatedActor(UUID.randomUUID(), UUID.randomUUID(),
                "admin@example.com", Set.of("ADMIN"), Set.of("CUSTOMER_CREATE"), null));
    }

    @Test
    void requiresAddressWhenCreatingCustomer() {
        assertThatThrownBy(() -> service.create(command("  ")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("endereço");

        verify(repository, never()).save(any());
    }

    @Test
    void trimsAndPersistsCustomerAddress() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(command("  Rua das Flores, 100 - Centro  "));

        var saved = ArgumentCaptor.forClass(Customer.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().address()).isEqualTo("Rua das Flores, 100 - Centro");
    }

    private CustomerUseCase.CreateCustomerCommand command(String address) {
        return new CustomerUseCase.CreateCustomerCommand(CustomerType.PERSON, "Maria da Silva",
                "12345678901", "maria@example.com", "11999999999", address, null);
    }
}
