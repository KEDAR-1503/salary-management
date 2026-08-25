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

export interface UpdateSalaryRequest {
  newSalary: number;
  effectiveDate: string;
  reason: string;
}
