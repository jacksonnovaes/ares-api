package br.com.ares.tenant.application.port.in;

import br.com.ares.asset.domain.model.Asset;
import br.com.ares.customer.domain.model.Customer;
import br.com.ares.identity.domain.model.Permission;
import br.com.ares.identity.domain.model.Role;
import br.com.ares.identity.domain.model.UserStatus;
import br.com.ares.servicecatalog.domain.model.CatalogService;
import br.com.ares.serviceorder.domain.model.ServiceOrder;
import br.com.ares.shared.application.AuditLogPort;
import br.com.ares.tenant.domain.model.Tenant;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PrivacyUseCase {

    TenantDataExport exportData();

    DataDeletionResult deleteAccount(DeleteAccountCommand command);

    record TenantDataExport(String formatVersion, Instant exportedAt, Tenant company,
                            List<UserData> users, List<Customer> customers, List<Asset> assets,
                            List<CatalogService> catalogServices, List<ServiceOrder> serviceOrders,
                            List<AuditLogPort.AuditEventView> auditTrail) {
    }

    record UserData(UUID id, UUID customerId, String name, String email, String phone, String jobTitle,
                    UserStatus status, Set<Role> roles, Set<Permission> permissions,
                    Instant lastLoginAt, Instant passwordChangedAt, Instant createdAt, Instant updatedAt) {
    }

    record DeleteAccountCommand(String currentPassword, String confirmation) {
    }

    record DataDeletionResult(UUID receiptId, Instant deletedAt) {
    }
}
