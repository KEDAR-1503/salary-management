import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CreateEmployeeComponent } from './create-employee.component';
import { EmployeeService } from '../../core/services/employee.service';
import { Employee } from '../../core/models/employee.model';

describe('CreateEmployeeComponent', () => {
  let fixture: ComponentFixture<CreateEmployeeComponent>;
  let employeeService: jasmine.SpyObj<EmployeeService>;

  const createdEmployee: Employee = {
    id: 42,
    version: 0,
    employeeIdentifier: 'EMP-2001',
    fullName: 'Dana Lee',
    email: 'dana.lee@acme.corp',
    department: 'Product',
    roleTitle: 'Staff Level 2',
    country: 'Singapore',
    currency: 'SGD',
    currentSalary: 98000,
    effectiveDate: '2026-08-27'
  };

  beforeEach(async () => {
    employeeService = jasmine.createSpyObj('EmployeeService', ['createEmployee', 'getFilterOptions']);
    employeeService.getFilterOptions.and.returnValue(of({
      departments: ['Engineering', 'Product', 'Sales'],
      countries: ['United States', 'Singapore', 'India'],
      roleTitles: ['Staff Level 1', 'Staff Level 2', 'Staff Level 3']
    }));
    employeeService.createEmployee.and.returnValue(of(createdEmployee));

    await TestBed.configureTestingModule({
      imports: [CreateEmployeeComponent],
      providers: [
        provideRouter([]),
        { provide: EmployeeService, useValue: employeeService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateEmployeeComponent);
    fixture.detectChanges();
  });

  it('should load catalog options on init for dropdowns', () => {
    expect(employeeService.getFilterOptions).toHaveBeenCalled();
    expect(fixture.componentInstance.departments()).toEqual(['Engineering', 'Product', 'Sales']);
    expect(fixture.componentInstance.countries()).toEqual(['United States', 'Singapore', 'India']);
    expect(fixture.componentInstance.roleTitles()).toEqual(['Staff Level 1', 'Staff Level 2', 'Staff Level 3']);
  });

  it('should render select dropdowns for department, role, country, and currency', () => {
    const selects = fixture.nativeElement.querySelectorAll('select');
    expect(selects.length).toBeGreaterThanOrEqual(4);

    const departmentSelect: HTMLSelectElement = fixture.nativeElement.querySelector('select[formControlName="department"]');
    const roleSelect: HTMLSelectElement = fixture.nativeElement.querySelector('select[formControlName="roleTitle"]');
    const countrySelect: HTMLSelectElement = fixture.nativeElement.querySelector('select[formControlName="country"]');
    const currencySelect: HTMLSelectElement = fixture.nativeElement.querySelector('select[formControlName="currency"]');

    expect(departmentSelect).toBeTruthy();
    expect(roleSelect).toBeTruthy();
    expect(countrySelect).toBeTruthy();
    expect(currencySelect).toBeTruthy();

    expect(Array.from(departmentSelect.options).map(o => o.value)).toContain('Engineering');
    expect(Array.from(roleSelect.options).map(o => o.value)).toContain('Staff Level 1');
    expect(Array.from(countrySelect.options).map(o => o.value)).toContain('Singapore');
  });

  it('should auto-set currency when country changes', () => {
    const component = fixture.componentInstance;
    component.onCountryChange('Singapore');
    expect(component.form.controls.currency.value).toBe('SGD');

    component.onCountryChange('United States');
    expect(component.form.controls.currency.value).toBe('USD');

    component.onCountryChange('India');
    expect(component.form.controls.currency.value).toBe('INR');
  });

  it('should keep currency select disabled so only country drives currency', () => {
    const currencySelect: HTMLSelectElement = fixture.nativeElement.querySelector('select[formControlName="currency"]');
    expect(currencySelect.disabled).toBeTrue();
  });

  it('should submit selected catalog values via createEmployee', () => {
    const component = fixture.componentInstance;
    component.form.setValue({
      employeeIdentifier: 'EMP-2001',
      fullName: 'Dana Lee',
      email: 'dana.lee@acme.corp',
      department: 'Product',
      roleTitle: 'Staff Level 2',
      country: 'Singapore',
      currency: 'SGD',
      initialSalary: 98000,
      effectiveDate: '2026-08-27'
    });
    component.onSubmit();

    expect(employeeService.createEmployee).toHaveBeenCalledWith(jasmine.objectContaining({
      department: 'Product',
      roleTitle: 'Staff Level 2',
      country: 'Singapore',
      currency: 'SGD'
    }));
  });

  it('should show error when create fails', () => {
    employeeService.createEmployee.and.returnValue(throwError(() => new Error('fail')));
    const component = fixture.componentInstance;
    component.form.patchValue({
      employeeIdentifier: 'EMP-2001',
      fullName: 'Dana Lee',
      email: 'dana.lee@acme.corp',
      department: 'Product',
      roleTitle: 'Staff Level 2',
      country: 'Singapore',
      currency: 'SGD',
      initialSalary: 98000
    });
    component.onSubmit();
    fixture.detectChanges();

    expect(component.error()).toContain('Failed');
  });
});
