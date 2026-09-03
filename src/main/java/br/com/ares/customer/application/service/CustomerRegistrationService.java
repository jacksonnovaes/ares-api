package br.com.ares.customer.application.service;

import br.com.ares.customer.application.port.in.CustomerRegistrationUseCase;
import br.com.ares.customer.application.port.in.CustomerUseCase;
import br.com.ares.customer.domain.model.Customer;
import br.com.ares.identity.application.port.in.UserManagementUseCase;
import br.com.ares.identity.domain.model.Role;
import br.com.ares.shared.application.CurrentActorProvider;
import br.com.ares.shared.domain.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class CustomerRegistrationService implements CustomerRegistrationUseCase {

    private final CustomerUseCase customers;
    private final UserManagementUseCase users;
    private final CurrentActorProvider currentActor;

    public CustomerRegistrationService(CustomerUseCase customers, UserManagementUseCase users,
                                       CurrentActorProvider currentActor) {
        this.customers = customers;
        this.users = users;
        this.currentActor = currentActor;
    }

    @Override
    @Transactional
    public Customer create(CreateCustomerRegistrationCommand command) {
        if (command.createUserAccess()) {
            var actor = currentActor.requiredActor();
            if (!actor.permissions().contains("USER_MANAGE")) {
                throw BusinessException.forbidden("customer_user_access_forbidden",
                        "Você não possui permissão para criar o acesso deste cliente.");
            }
            if (command.email() == null || command.email().isBlank()) {
                throw BusinessException.badRequest("customer_user_email_required",
                        "Informe o e-mail do cliente para criar o acesso ao portal.");
            }
        }

        Customer customer = customers.create(new CustomerUseCase.CreateCustomerCommand(
                command.type(), command.name(), command.document(), command.email(), command.phone(), command.address(),
                command.notes()));

        if (command.createUserAccess()) {
            users.create(new UserManagementUseCase.CreateUserCommand(
                    customer.name(), customer.email(), customer.phone(), "Cliente",
                    command.password(), command.passwordConfirmation(), Set.of(Role.CUSTOMER), Set.of(),
                    customer.id()));
        }
        return customer;
    }
}
