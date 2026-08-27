import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Employee, EmployeeFilterOptions, EmployeeFilterParams, PaginatedResponse, UpdateSalaryRequest } from '../models/employee.model';

export class SalaryConflictError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'SalaryConflictError';
  }
}

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/employees';

  getEmployees(filters: EmployeeFilterParams = {}): Observable<PaginatedResponse<Employee>> {
    let params: Record<string, string> = {};
    if (filters.page !== undefined) params['page'] = filters.page.toString();
    if (filters.size !== undefined) params['size'] = filters.size.toString();
    if (filters.department) params['department'] = filters.department;
    if (filters.country) params['country'] = filters.country;
    if (filters.search) params['search'] = filters.search;
    return this.http.get<PaginatedResponse<Employee>>(this.baseUrl, { params });
  }

  getFilterOptions(): Observable<EmployeeFilterOptions> {
    return this.http.get<EmployeeFilterOptions>(`${this.baseUrl}/filter-options`);
  }

  getEmployee(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.baseUrl}/${id}`);
  }

  createEmployee(request: {
    fullName: string;
    email: string;
    department: string;
    roleTitle: string;
    country: string;
    currency: string;
    initialSalary: number;
  }): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, request);
  }

  updateSalary(id: number, request: UpdateSalaryRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/${id}/salary`, request).pipe(
      catchError((err: HttpErrorResponse) => {
        if (err.status === 409) {
          return throwError(() => new SalaryConflictError(
            err.error?.detail ?? 'This record was modified by someone else. Please reload and try again.'
          ));
        }
        return throwError(() => err);
      })
    );
  }

  getHistory(id: number): Observable<import('../models/employee.model').SalaryAuditLog[]> {
    return this.http.get<import('../models/employee.model').SalaryAuditLog[]>(`${this.baseUrl}/${id}/history`);
  }
}
