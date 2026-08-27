package br.com.ares.identity.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RolePermissionsCustomerTest {

    @Test
    void grantsOnlyOrderReadingToCustomerAccounts() {
        assertThat(RolePermissions.defaultsFor(Set.of(Role.CUSTOMER)))
                .containsExactly(Permission.SERVICE_ORDER_READ);
    }
}
