import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EmployeeDirectoryComponent } from './employee-directory.component';
import { EmployeeService } from '../../core/services/employee.service';
import { Employee, PaginatedResponse } from '../../core/models/employee.model';

describe('EmployeeDirectoryComponent', () => {
  let fixture: ComponentFixture<EmployeeDirectoryComponent>;
  let employeeService: jasmine.SpyObj<EmployeeService>;

  const pageResponse: PaginatedResponse<Employee> = {
    content: [{
      id: 1,
      version: 0,
      employeeIdentifier: 'EMP-00001',
      fullName: 'Worker 1',
      email: 'worker.00001@acme.corp',
      department: 'Engineering',
      roleTitle: 'Staff',
      country: 'United States',
      currency: 'USD',
      currentSalary: 100000,
      effectiveDate: '2026-08-26'
    }],
    totalElements: 1,
    totalPages: 3,
    size: 20,
    number: 0
  };

  beforeEach(async () => {
    employeeService = jasmine.createSpyObj('EmployeeService', ['getEmployees', 'getFilterOptions']);
    employeeService.getFilterOptions.and.returnValue(of({
      departments: ['Engineering', 'Finance'],
      countries: ['India', 'United States']
    }));
    employeeService.getEmployees.and.returnValue(of(pageResponse));

    await TestBed.configureTestingModule({
      imports: [EmployeeDirectoryComponent],
      providers: [
        provideRouter([]),
        { provide: EmployeeService, useValue: employeeService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeDirectoryComponent);
    fixture.detectChanges();
  });

  it('should load filter options and first page of employees on init', () => {
    expect(employeeService.getFilterOptions).toHaveBeenCalled();
    expect(employeeService.getEmployees).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      search: undefined,
      department: undefined,
      country: undefined
    });
    expect(fixture.componentInstance.departments()).toEqual(['Engineering', 'Finance']);
    expect(fixture.componentInstance.countries()).toEqual(['India', 'United States']);
    expect(fixture.nativeElement.textContent).toContain('EMP-00001');
    expect(fixture.nativeElement.textContent).toContain('Worker 1');
  });

  it('should reset to page 0 and pass selected department filter', () => {
    const component = fixture.componentInstance;
    component.page.set(2);
    component.department = 'Engineering';
    component.applyFilters();

    expect(component.page()).toBe(0);
    expect(employeeService.getEmployees).toHaveBeenCalledWith(jasmine.objectContaining({
      page: 0,
      department: 'Engineering'
    }));
  });

  it('should show error when employee list request fails', () => {
    employeeService.getEmployees.and.returnValue(throwError(() => new Error('boom')));
    fixture.componentInstance.load();
    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBe('Failed to load employees.');
    expect(fixture.nativeElement.textContent).toContain('Failed to load employees.');
  });

  it('should request next page when Next is clicked', () => {
    fixture.componentInstance.nextPage();
    expect(employeeService.getEmployees).toHaveBeenCalledWith(jasmine.objectContaining({ page: 1 }));
  });
});
