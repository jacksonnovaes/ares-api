package br.com.ares.identity.domain.model;

import java.util.EnumSet;
import java.util.Set;

public final class RolePermissions {

    private RolePermissions() {
    }

    public static Set<Permission> defaultsFor(Set<Role> roles) {
        EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
        roles.forEach(role -> permissions.addAll(forRole(role)));
        return Set.copyOf(permissions);
    }

    private static Set<Permission> forRole(Role role) {
        return switch (role) {
            case SUPER_ADMIN, ADMIN -> EnumSet.allOf(Permission.class);
            case MANAGER -> EnumSet.of(
                    Permission.CUSTOMER_READ, Permission.CUSTOMER_CREATE, Permission.CUSTOMER_UPDATE,
                    Permission.ASSET_READ, Permission.ASSET_CREATE, Permission.ASSET_UPDATE,
                    Permission.SERVICE_READ, Permission.SERVICE_CREATE, Permission.SERVICE_UPDATE,
                    Permission.SERVICE_ORDER_READ, Permission.SERVICE_ORDER_CREATE,
                    Permission.SERVICE_ORDER_UPDATE, Permission.SERVICE_ORDER_CANCEL,
                    Permission.REPORT_READ);
            case ATTENDANT -> EnumSet.of(
                    Permission.CUSTOMER_READ, Permission.CUSTOMER_CREATE, Permission.CUSTOMER_UPDATE,
                    Permission.ASSET_READ, Permission.ASSET_CREATE, Permission.ASSET_UPDATE,
                    Permission.SERVICE_READ, Permission.SERVICE_ORDER_READ,
                    Permission.SERVICE_ORDER_CREATE, Permission.SERVICE_ORDER_UPDATE);
            case TECHNICIAN -> EnumSet.of(
                    Permission.CUSTOMER_READ, Permission.ASSET_READ, Permission.SERVICE_READ,
                    Permission.SERVICE_ORDER_READ, Permission.SERVICE_ORDER_UPDATE);
            case FINANCIAL -> EnumSet.of(
                    Permission.PAYMENT_READ, Permission.PAYMENT_CREATE, Permission.REPORT_READ);
            case CUSTOMER -> EnumSet.of(
                    Permission.ASSET_READ, Permission.SERVICE_ORDER_READ, Permission.PAYMENT_READ);
        };
    }
}
