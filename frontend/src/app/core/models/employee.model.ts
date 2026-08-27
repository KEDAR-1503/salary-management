export interface Employee {
  id: number;
  version: number;
  employeeIdentifier: string;
  fullName: string;
  email: string;
  department: string;
  roleTitle: string;
  country: string;
  currency: string;
  currentSalary: number;
  effectiveDate: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface EmployeeFilterParams {
  page?: number;
  size?: number;
  department?: string;
  country?: string;
  search?: string;
}

export interface EmployeeFilterOptions {
  departments: string[];
  countries: string[];
  roleTitles: string[];
}

export interface UpdateSalaryRequest {
  version: number;
  newSalary: number;
  effectiveDate: string;
  reason: string;
}

export interface SalaryAuditLog {
  id: number;
  employeeId: number;
  previousSalary: number | null;
  newSalary: number;
  currency: string;
  changedBy: string;
  reason: string;
  changedAt: string;
}

export interface CreateEmployeeRequest {
  fullName: string;
  email: string;
  department: string;
  roleTitle: string;
  country: string;
  currency: string;
  initialSalary: number;
}
