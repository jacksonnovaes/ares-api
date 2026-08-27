package br.com.ares.identity.application.service;

import br.com.ares.customer.application.port.in.CustomerDirectory;
import br.com.ares.identity.application.port.in.UserManagementUseCase;
import br.com.ares.identity.application.port.out.PasswordHasher;
import br.com.ares.identity.application.port.out.RefreshSessionRepository;
import br.com.ares.identity.application.port.out.UserRepository;
import br.com.ares.identity.domain.model.Permission;
import br.com.ares.identity.domain.model.Role;
import br.com.ares.identity.domain.service.PasswordPolicy;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.shared.application.AuthenticatedActor;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceCustomerAccessTest {

    @Mock UserRepository users;
    @Mock RefreshSessionRepository sessions;
    @Mock PasswordHasher passwordHasher;
    @Mock PasswordPolicy passwordPolicy;
    @Mock CustomerDirectory customers;
    @Mock CurrentActorProvider currentActor;
    @Mock AuditLogPort audit;

    private UserManagementService service;
    private UUID tenantId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        service = new UserManagementService(users, sessions, passwordHasher, passwordPolicy, customers,
                currentActor, audit, Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void createsCustomerWithOnlyOrderReadPermission() {
        prepareValidCustomer();
        when(passwordHasher.hash("SenhaForte#123")).thenReturn("hash");
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(command(Set.of(Role.CUSTOMER), Set.of()));

        assertThat(created.roles()).containsExactly(Role.CUSTOMER);
        assertThat(created.permissions()).containsExactly(Permission.SERVICE_ORDER_READ);
        assertThat(created.customerId()).isEqualTo(customerId);
    }

    @Test
    void rejectsCustomerCombinedWithAStaffRole() {
        prepareValidCustomer();

        assertThatThrownBy(() -> service.create(command(Set.of(Role.CUSTOMER, Role.ATTENDANT), Set.of())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("somente o perfil CUSTOMER");
    }

    @Test
    void rejectsAdditionalPermissionForCustomer() {
        prepareValidCustomer();

        assertThatThrownBy(() -> service.create(command(Set.of(Role.CUSTOMER), Set.of(Permission.ASSET_READ))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permissões adicionais");
    }

    private void prepareValidCustomer() {
        when(currentActor.requiredActor()).thenReturn(new AuthenticatedActor(UUID.randomUUID(), tenantId,
                "admin@example.com", Set.of("ADMIN"), Set.of("USER_MANAGE"), null));
        when(customers.exists(tenantId, customerId)).thenReturn(true);
    }

    private UserManagementUseCase.CreateUserCommand command(Set<Role> roles, Set<Permission> permissions) {
        return new UserManagementUseCase.CreateUserCommand("Cliente Teste", "cliente@example.com", null,
                "Cliente", "SenhaForte#123", "SenhaForte#123", roles, permissions, customerId);
    }
}
