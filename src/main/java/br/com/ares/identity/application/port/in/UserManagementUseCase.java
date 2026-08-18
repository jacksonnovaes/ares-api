package br.com.ares.identity.application.port.in;

import br.com.ares.identity.domain.model.Permission;
import br.com.ares.identity.domain.model.Role;
import br.com.ares.identity.domain.model.UserStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserManagementUseCase {
    UserView create(CreateUserCommand command);
    List<UserView> list();
    UserView changeStatus(UUID id, UserStatus status);

    record CreateUserCommand(String name, String email, String phone, String jobTitle,
                             String password, String passwordConfirmation, Set<Role> roles,
                             Set<Permission> extraPermissions, UUID customerId) {
    }

    record UserView(UUID id, String name, String email, String phone, String jobTitle,
                    UserStatus status, Set<Role> roles, Set<Permission> permissions, UUID customerId) {
    }
}
