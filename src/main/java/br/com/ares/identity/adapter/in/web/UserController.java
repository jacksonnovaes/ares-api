package br.com.ares.identity.adapter.in.web;

import br.com.ares.identity.application.port.in.UserManagementUseCase;
import br.com.ares.identity.domain.model.Permission;
import br.com.ares.identity.domain.model.Role;
import br.com.ares.identity.domain.model.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserManagementUseCase users;

    public UserController(UserManagementUseCase users) {
        this.users = users;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    UserManagementUseCase.UserView create(@Valid @RequestBody CreateUserRequest request) {
        return users.create(new UserManagementUseCase.CreateUserCommand(
                request.name(), request.email(), request.phone(), request.jobTitle(), request.password(),
                request.passwordConfirmation(), request.roles(), request.extraPermissions(), request.customerId()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    List<UserManagementUseCase.UserView> list() {
        return users.list();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    UserManagementUseCase.UserView changeStatus(@PathVariable UUID id,
                                                @Valid @RequestBody ChangeStatusRequest request) {
        return users.changeStatus(id, request.status());
    }

    record CreateUserRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @Size(max = 30) String phone,
            @Size(max = 100) String jobTitle,
            @NotBlank String password,
            @NotBlank String passwordConfirmation,
            @NotEmpty Set<Role> roles,
            Set<Permission> extraPermissions,
            UUID customerId
    ) {
    }

    record ChangeStatusRequest(UserStatus status) {
    }
}
