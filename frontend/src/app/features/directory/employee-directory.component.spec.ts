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
      countries: ['India', 'United States'],
      roleTitles: ['Staff Level 1', 'Staff Level 2']
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

  it('should not advance past the last page', () => {
    const component = fixture.componentInstance;
    component.page.set(2);
    component.totalPages.set(3);
    employeeService.getEmployees.calls.reset();
    component.nextPage();
    expect(component.page()).toBe(2);
    expect(employeeService.getEmployees).not.toHaveBeenCalled();
  });

  it('should pass country and search filters together', () => {
    const component = fixture.componentInstance;
    component.country = 'India';
    component.search = 'EMP-00001';
    component.applyFilters();

    expect(employeeService.getEmployees).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      search: 'EMP-00001',
      department: undefined,
      country: 'India'
    });
  });

  it('should pass an exact email search term to the employee service (positive)', () => {
    const component = fixture.componentInstance;
    component.search = 'worker.00001@acme.corp';
    component.applyFilters();

    expect(employeeService.getEmployees).toHaveBeenCalledWith(jasmine.objectContaining({
      search: 'worker.00001@acme.corp',
      page: 0
    }));
  });

  it('should pass an exact full-name search term to the employee service (positive)', () => {
    const component = fixture.componentInstance;
    component.search = 'Worker 1';
    component.applyFilters();

    expect(employeeService.getEmployees).toHaveBeenCalledWith(jasmine.objectContaining({
      search: 'Worker 1'
    }));
  });

  it('should omit blank search from the request (negative)', () => {
    const component = fixture.componentInstance;
    component.search = '   ';
    component.applyFilters();

    expect(employeeService.getEmployees).toHaveBeenCalledWith(jasmine.objectContaining({
      search: undefined
    }));
  });

  it('should advertise exact search by name, email, or employee ID', () => {
    const input: HTMLInputElement = fixture.nativeElement.querySelector('.filters input');
    expect(input.placeholder.toLowerCase()).toContain('name');
    expect(input.placeholder.toLowerCase()).toContain('email');
    expect(input.placeholder.toLowerCase()).toMatch(/id|employee/);
  });

  it('should keep page at 0 when Previous is clicked on the first page', () => {
    const component = fixture.componentInstance;
    employeeService.getEmployees.calls.reset();
    expect(component.page()).toBe(0);
    component.prevPage();
    expect(component.page()).toBe(0);
    expect(employeeService.getEmployees).not.toHaveBeenCalled();
  });

  it('should wrap the results table for horizontal scrolling on small screens', () => {
    const wrap = fixture.nativeElement.querySelector('.table-wrap');
    expect(wrap).toBeTruthy();
    expect(wrap.querySelector('table')).toBeTruthy();
  });

  it('should render an empty table body when the page has no employees', () => {
    employeeService.getEmployees.and.returnValue(of({
      ...pageResponse,
      content: [],
      totalElements: 0,
      totalPages: 0
    }));
    fixture.componentInstance.load();
    fixture.detectChanges();

    expect(fixture.componentInstance.employees()).toEqual([]);
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(0);
  });
});
