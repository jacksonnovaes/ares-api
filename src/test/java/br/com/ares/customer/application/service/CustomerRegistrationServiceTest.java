package br.com.ares.customer.application.service;

import br.com.ares.customer.application.port.in.CustomerRegistrationUseCase;
import br.com.ares.customer.application.port.in.CustomerUseCase;
import br.com.ares.customer.domain.model.Customer;
import br.com.ares.customer.domain.model.CustomerStatus;
import br.com.ares.customer.domain.model.CustomerType;
import br.com.ares.identity.application.port.in.UserManagementUseCase;
import br.com.ares.identity.domain.model.Role;
import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerRegistrationServiceTest {

    @Mock CustomerUseCase customers;
    @Mock UserManagementUseCase users;
    @Mock CurrentActorProvider currentActor;

    private CustomerRegistrationService service;
    private Customer customer;

    @BeforeEach
    void setUp() {
        service = new CustomerRegistrationService(customers, users, currentActor);
        Instant now = Instant.parse("2026-08-30T12:00:00Z");
        customer = new Customer(UUID.randomUUID(), UUID.randomUUID(), CustomerType.PERSON,
                "Maria Silva", "12345678901", "maria@example.com", "11999999999",
                "Rua das Flores, 100 - Centro", null,
                CustomerStatus.ACTIVE, now, now);
    }

    @Test
    void createsTheCustomerAndItsReadOnlyPortalUser() {
        when(currentActor.requiredActor()).thenReturn(new AuthenticatedActor(UUID.randomUUID(), customer.tenantId(),
                "admin@example.com", Set.of("ADMIN"), Set.of("CUSTOMER_CREATE", "USER_MANAGE"), null));
        when(customers.create(any())).thenReturn(customer);

        Customer result = service.create(command(true));

        assertThat(result).isEqualTo(customer);
        var user = ArgumentCaptor.forClass(UserManagementUseCase.CreateUserCommand.class);
        verify(users).create(user.capture());
        assertThat(user.getValue().customerId()).isEqualTo(customer.id());
        assertThat(user.getValue().roles()).containsExactly(Role.CUSTOMER);
        assertThat(user.getValue().extraPermissions()).isEmpty();
        assertThat(user.getValue().email()).isEqualTo(customer.email());
    }

    @Test
    void createsOnlyTheCustomerWhenPortalAccessWasNotSelected() {
        when(customers.create(any())).thenReturn(customer);

        assertThat(service.create(command(false))).isEqualTo(customer);

        verify(users, never()).create(any());
    }

    @Test
    void rejectsPortalAccessWithoutUserManagementPermission() {
        when(currentActor.requiredActor()).thenReturn(new AuthenticatedActor(UUID.randomUUID(), customer.tenantId(),
                "attendant@example.com", Set.of("ATTENDANT"), Set.of("CUSTOMER_CREATE"), null));

        assertThatThrownBy(() -> service.create(command(true)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permissão");

        verify(customers, never()).create(any());
        verify(users, never()).create(any());
    }

    private CustomerRegistrationUseCase.CreateCustomerRegistrationCommand command(boolean createUserAccess) {
        return new CustomerRegistrationUseCase.CreateCustomerRegistrationCommand(CustomerType.PERSON,
                "Maria Silva", "12345678901", "maria@example.com", "11999999999",
                "Rua das Flores, 100 - Centro", null,
                createUserAccess, "SenhaForte#123", "SenhaForte#123");
    }
}
