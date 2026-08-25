import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EmployeeService } from './employee.service';
import { Employee, PaginatedResponse, UpdateSalaryRequest } from '../models/employee.model';

describe('EmployeeService', () => {
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

  afterEach(() => httpMock.verify());

  it('should fetch paginated employees with query params', () => {
    const mockResponse: PaginatedResponse<Employee> = {
      content: [{
        id: 1, version: 0, employeeIdentifier: 'EMP-00001', fullName: 'Alice Walker',
        email: 'alice@acme.corp', department: 'Engineering', roleTitle: 'Architect',
        country: 'United States', currency: 'USD', currentSalary: 150000, effectiveDate: '2026-08-25'
      }],
      totalElements: 1, totalPages: 1, size: 20, number: 0
    };

    service.getEmployees({ page: 0, size: 20, department: 'Engineering' }).subscribe(res => {
      expect(res.content.length).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url === '/api/v1/employees' && r.params.get('department') === 'Engineering');
    req.flush(mockResponse);
  });

  it('should send PUT with version on salary update', () => {
    const payload: UpdateSalaryRequest = {
      version: 0,
      newSalary: 160000,
      effectiveDate: '2026-08-25',
      reason: 'Annual merit increase adjustment'
    };

    service.updateSalary(1, payload).subscribe();
    const req = httpMock.expectOne('/api/v1/employees/1/salary');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.version).toBe(0);
    req.flush({ ...payload, id: 1, employeeIdentifier: 'EMP-1', fullName: 'A', email: 'a@b.c',
      department: 'Eng', roleTitle: 'Dev', country: 'US', currency: 'USD', currentSalary: 160000 });
  });

  it('should map 409 to SalaryConflictError', (done) => {
    service.updateSalary(1, { version: 0, newSalary: 1, effectiveDate: '2026-08-25', reason: 'long enough reason' })
      .subscribe({
        error: err => {
          expect(err.name).toBe('SalaryConflictError');
          done();
        }
      });
    const req = httpMock.expectOne('/api/v1/employees/1/salary');
    req.flush({ detail: 'Conflict' }, { status: 409, statusText: 'Conflict' });
  });
});
