import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Employee, EmployeeFilterParams, PaginatedResponse, UpdateSalaryRequest } from '../models/employee.model';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/employees';

  getEmployees(filters: EmployeeFilterParams = {}): Observable<PaginatedResponse<Employee>> {
    let params = new HttpParams();
    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.department) params = params.set('department', filters.department);
    if (filters.country) params = params.set('country', filters.country);
    if (filters.search) params = params.set('search', filters.search);

    return this.http.get<PaginatedResponse<Employee>>(this.baseUrl, { params });
  }

  updateSalary(id: number, request: UpdateSalaryRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/${id}/salary`, request);
  }
}
