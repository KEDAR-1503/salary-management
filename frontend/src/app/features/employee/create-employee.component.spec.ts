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
    employeeIdentifier: 'EMP-10001',
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

  it('should load catalog options on init for dropdowns (positive)', () => {
    expect(employeeService.getFilterOptions).toHaveBeenCalled();
    expect(fixture.componentInstance.departments()).toEqual(['Engineering', 'Product', 'Sales']);
    expect(fixture.componentInstance.countries()).toEqual(['United States', 'Singapore', 'India']);
    expect(fixture.componentInstance.roleTitles()).toEqual(['Staff Level 1', 'Staff Level 2', 'Staff Level 3']);
  });

  it('should not render an Employee ID input (positive)', () => {
    const idInput = fixture.nativeElement.querySelector('input[formControlName="employeeIdentifier"]');
    expect(idInput).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Employee ID');
  });

  it('should default effective date to today and keep it editable (positive)', () => {
    const dateInput: HTMLInputElement = fixture.nativeElement.querySelector('input[formControlName="effectiveDate"]');
    expect(dateInput).toBeTruthy();
    expect(dateInput.disabled).toBeFalse();
    expect(dateInput.value).toBe(fixture.componentInstance.today);
    expect(dateInput.getAttribute('min')).toBe(fixture.componentInstance.today);
  });

  it('should reject past effective dates in the form (negative)', () => {
    const component = fixture.componentInstance;
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    component.form.controls.effectiveDate.setValue(yesterday.toISOString().slice(0, 10));
    expect(component.form.controls.effectiveDate.invalid).toBeTrue();
    expect(component.form.controls.effectiveDate.errors).toEqual(jasmine.objectContaining({ pastDate: true }));
  });

  it('should accept a future effective date (positive)', () => {
    const component = fixture.componentInstance;
    const future = new Date();
    future.setDate(future.getDate() + 5);
    const futureIso = future.toISOString().slice(0, 10);
    component.form.controls.effectiveDate.setValue(futureIso);
    expect(component.form.controls.effectiveDate.valid).toBeTrue();
  });

  it('should render select dropdowns for department, role, country, and currency (positive)', () => {
    const departmentSelect: HTMLSelectElement = fixture.nativeElement.querySelector('select[formControlName="department"]');
    const roleSelect: HTMLSelectElement = fixture.nativeElement.querySelector('select[formControlName="roleTitle"]');
    const countrySelect: HTMLSelectElement = fixture.nativeElement.querySelector('select[formControlName="country"]');
    const currencySelect: HTMLSelectElement = fixture.nativeElement.querySelector('select[formControlName="currency"]');

    expect(departmentSelect).toBeTruthy();
    expect(roleSelect).toBeTruthy();
    expect(countrySelect).toBeTruthy();
    expect(currencySelect).toBeTruthy();
    expect(currencySelect.disabled).toBeTrue();
  });

  it('should auto-set currency when country changes (positive)', () => {
    const component = fixture.componentInstance;
    component.onCountryChange('Singapore');
    expect(component.form.controls.currency.value).toBe('SGD');
    component.onCountryChange('India');
    expect(component.form.controls.currency.value).toBe('INR');
  });

  it('should submit without employeeIdentifier and include effectiveDate (positive)', () => {
    const component = fixture.componentInstance;
    component.form.patchValue({
      fullName: 'Dana Lee',
      email: 'dana.lee@acme.corp',
      department: 'Product',
      roleTitle: 'Staff Level 2',
      country: 'Singapore',
      initialSalary: 98000,
      effectiveDate: component.today
    });
    component.onCountryChange('Singapore');
    component.onSubmit();

    expect(employeeService.createEmployee).toHaveBeenCalled();
    const payload = employeeService.createEmployee.calls.mostRecent().args[0];
    expect(payload.fullName).toBe('Dana Lee');
    expect(payload.department).toBe('Product');
    expect(payload.currency).toBe('SGD');
    expect(payload.effectiveDate).toBe(component.today);
    expect((payload as { employeeIdentifier?: string }).employeeIdentifier).toBeUndefined();
  });

  it('should keep Create disabled when required fields are missing (negative)', () => {
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(fixture.componentInstance.form.invalid).toBeTrue();
    expect(button.disabled).toBeTrue();
  });

  it('should show error when create fails (negative)', () => {
    employeeService.createEmployee.and.returnValue(throwError(() => new Error('fail')));
    const component = fixture.componentInstance;
    component.form.patchValue({
      fullName: 'Dana Lee',
      email: 'dana.lee@acme.corp',
      department: 'Product',
      roleTitle: 'Staff Level 2',
      country: 'Singapore',
      initialSalary: 98000
    });
    component.onCountryChange('Singapore');
    component.onSubmit();
    fixture.detectChanges();

    expect(component.error()).toContain('Failed');
  });
});
