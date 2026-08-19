export type Role =
  | "SUPER_ADMIN"
  | "ADMIN"
  | "MANAGER"
  | "ATTENDANT"
  | "TECHNICIAN"
  | "FINANCIAL"
  | "CUSTOMER";

export type Permission =
  | "CUSTOMER_READ"
  | "CUSTOMER_CREATE"
  | "CUSTOMER_UPDATE"
  | "CUSTOMER_DELETE"
  | "ASSET_READ"
  | "ASSET_CREATE"
  | "ASSET_UPDATE"
  | "ASSET_DELETE"
  | "SERVICE_READ"
  | "SERVICE_CREATE"
  | "SERVICE_UPDATE"
  | "SERVICE_DELETE"
  | "SERVICE_ORDER_READ"
  | "SERVICE_ORDER_CREATE"
  | "SERVICE_ORDER_UPDATE"
  | "SERVICE_ORDER_CANCEL"
  | "PAYMENT_READ"
  | "PAYMENT_CREATE"
  | "REPORT_READ"
  | "USER_MANAGE"
  | "TENANT_CONFIGURE";

export interface TenantSummary {
  id: string;
  name: string;
  slug: string;
}

export interface Me {
  id: string;
  name: string;
  email: string;
  tenant: TenantSummary;
  roles: Role[];
  permissions: Permission[];
}

export interface AuthenticationResult {
  expiresIn: number;
  user: {
    id: string;
    name: string;
    tenantId: string;
    roles: Role[];
  };
}

export interface Branding {
  tradeName: string;
  slug: string;
  logoUrl?: string | null;
  primaryColor?: string | null;
}

export type CustomerType = "PERSON" | "COMPANY";
export type CustomerStatus = "ACTIVE" | "INACTIVE";

export interface Customer {
  id: string;
  tenantId: string;
  type: CustomerType;
  name: string;
  document?: string;
  email?: string;
  phone?: string;
  notes?: string;
  status: CustomerStatus;
  createdAt: string;
  updatedAt: string;
}

export type AssetType = "VEHICLE" | "PHONE" | "COMPUTER" | "EQUIPMENT" | "PROPERTY" | "OTHER";

export interface Asset {
  id: string;
  tenantId: string;
  customerId: string;
  type: AssetType;
  name: string;
  brand?: string;
  model?: string;
  serialNumber?: string;
  attributes?: Record<string, string>;
  createdAt: string;
  updatedAt: string;
}

export interface CatalogService {
  id: string;
  tenantId: string;
  name: string;
  description?: string;
  basePrice: number;
  estimatedMinutes?: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export type ServiceOrderPriority = "LOW" | "NORMAL" | "HIGH" | "URGENT";
export type ServiceOrderStatus =
  | "OPEN"
  | "IN_DIAGNOSIS"
  | "WAITING_APPROVAL"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "CANCELLED";

export interface ServiceOrder {
  id: string;
  tenantId: string;
  customerId: string;
  assetId: string;
  serviceIds: string[];
  title: string;
  description?: string;
  status: ServiceOrderStatus;
  priority: ServiceOrderPriority;
  estimatedValue?: number;
  finalValue?: number;
  assignedTechnicianId?: string;
  openedAt: string;
  dueAt?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export type UserStatus = "PENDING" | "ACTIVE" | "BLOCKED" | "INACTIVE";

export interface ManagedUser {
  id: string;
  name: string;
  email: string;
  phone?: string;
  jobTitle?: string;
  status: UserStatus;
  roles: Role[];
  permissions: Permission[];
  customerId?: string;
}

export interface ApiProblem {
  title?: string;
  detail?: string;
  message?: string;
  code?: string;
  status?: number;
  errors?: Record<string, string>;
}
