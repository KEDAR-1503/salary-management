import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EmployeeService } from './employee.service';
import { Employee, PaginatedResponse, UpdateSalaryRequest } from '../models/employee.model';

describe('EmployeeService (TDD)', () => {
  let service: EmployeeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EmployeeService]
    });
    service = TestBed.inject(EmployeeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch paginated employees with query params', () => {
    const mockResponse: PaginatedResponse<Employee> = {
      content: [
        {
          id: 1,
          version: 0,
          employeeIdentifier: 'EMP-00001',
          fullName: 'Alice Walker',
          email: 'alice.walker@acme.corp',
          department: 'Engineering',
          roleTitle: 'Architect',
          country: 'United States',
          currency: 'USD',
          currentSalary: 150000.00,
          effectiveDate: '2026-08-25'
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 20,
      number: 0
    };

    service.getEmployees({ page: 0, size: 20, department: 'Engineering' }).subscribe(res => {
      expect(res.content.length).toBe(1);
      expect(res.content[0].employeeIdentifier).toBe('EMP-00001');
    });

    const req = httpMock.expectOne(r => r.url === '/api/v1/employees' && r.params.get('department') === 'Engineering');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should send PUT request to update employee salary with meaningful reason', () => {
    const payload: UpdateSalaryRequest = {
      newSalary: 160000.00,
      effectiveDate: '2026-08-25',
      reason: 'Annual merit increase adjustment'
    };

    const mockUpdated: Employee = {
      id: 1,
      version: 1,
      employeeIdentifier: 'EMP-00001',
      fullName: 'Alice Walker',
      email: 'alice.walker@acme.corp',
      department: 'Engineering',
      roleTitle: 'Architect',
      country: 'United States',
      currency: 'USD',
      currentSalary: 160000.00,
      effectiveDate: '2026-08-25'
    };

    service.updateSalary(1, payload).subscribe(res => {
      expect(res.currentSalary).toBe(160000.00);
      expect(res.version).toBe(1);
    });

    const req = httpMock.expectOne('/api/v1/employees/1/salary');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush(mockUpdated);
  });
});
