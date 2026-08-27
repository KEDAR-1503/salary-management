import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EmployeeDetailComponent } from './employee-detail.component';
import { EmployeeService, SalaryConflictError } from '../../core/services/employee.service';
import { Employee, SalaryAuditLog } from '../../core/models/employee.model';

describe('EmployeeDetailComponent', () => {
  let fixture: ComponentFixture<EmployeeDetailComponent>;
  let employeeService: jasmine.SpyObj<EmployeeService>;

  const employee: Employee = {
    id: 2,
    version: 0,
    employeeIdentifier: 'EMP-00002',
    fullName: 'Worker 2',
    email: 'worker.00002@acme.corp',
    department: 'Finance',
    roleTitle: 'Staff Level 1',
    country: 'Singapore',
    currency: 'SGD',
    currentSalary: 167525,
    effectiveDate: '2026-08-26'
  };

  const history: SalaryAuditLog[] = [{
    id: 1,
    employeeId: 2,
    previousSalary: null,
    newSalary: 167525,
    currency: 'SGD',
    changedBy: 'system_seeder',
    reason: 'Initial Employee Setup',
    changedAt: '2026-08-26T10:00:00Z'
  }];

  beforeEach(async () => {
    employeeService = jasmine.createSpyObj('EmployeeService', ['getEmployee', 'getHistory', 'updateSalary']);
    employeeService.getEmployee.and.returnValue(of(employee));
    employeeService.getHistory.and.returnValue(of(history));
    employeeService.updateSalary.and.returnValue(of({ ...employee, currentSalary: 170000, version: 1 }));

    await TestBed.configureTestingModule({
      imports: [EmployeeDetailComponent],
      providers: [
        provideRouter([]),
        { provide: EmployeeService, useValue: employeeService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '2' }) } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeDetailComponent);
    fixture.detectChanges();
  });

  it('should show employee details including EMP ID (positive)', () => {
    expect(fixture.nativeElement.textContent).toContain('Worker 2');
    expect(fixture.nativeElement.textContent).toContain('EMP-00002');
    expect(fixture.nativeElement.textContent).toContain('Singapore');
  });

  it('should label new salary with the employee currency in brackets (positive)', () => {
    expect(fixture.nativeElement.textContent).toContain('New Salary (in SGD)');
  });

  it('should default effective date to today and keep it editable (positive)', () => {
    const dateInput: HTMLInputElement = fixture.nativeElement.querySelector('input[formControlName="effectiveDate"]');
    expect(dateInput.disabled).toBeFalse();
    expect(dateInput.value).toBe(fixture.componentInstance.today);
    expect(dateInput.getAttribute('min')).toBe(fixture.componentInstance.today);
  });

  it('should reject past effective dates in the salary form (negative)', () => {
    const component = fixture.componentInstance;
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    component.salaryForm.controls.effectiveDate.setValue(yesterday.toISOString().slice(0, 10));
    expect(component.salaryForm.controls.effectiveDate.invalid).toBeTrue();
  });

  it('should submit salary update with chosen effective date (positive)', () => {
    const component = fixture.componentInstance;
    const future = new Date();
    future.setDate(future.getDate() + 3);
    const futureIso = future.toISOString().slice(0, 10);
    component.salaryForm.patchValue({
      newSalary: 170000,
      effectiveDate: futureIso,
      reason: 'Annual merit performance promotion'
    });
    component.onSalarySubmit();

    expect(employeeService.updateSalary).toHaveBeenCalledWith(2, jasmine.objectContaining({
      version: 0,
      newSalary: 170000,
      effectiveDate: futureIso,
      reason: 'Annual merit performance promotion'
    }));
  });

  it('should keep Update disabled when reason is too short (negative)', () => {
    const component = fixture.componentInstance;
    component.salaryForm.patchValue({ reason: 'short' });
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(component.salaryForm.invalid).toBeTrue();
    expect(button.disabled).toBeTrue();
  });

  it('should show conflict message on optimistic lock failure (negative)', () => {
    employeeService.updateSalary.and.returnValue(
      throwError(() => new SalaryConflictError('Please reload and try again.'))
    );
    const component = fixture.componentInstance;
    component.salaryForm.patchValue({
      newSalary: 170000,
      reason: 'Annual merit performance promotion'
    });
    component.onSalarySubmit();
    fixture.detectChanges();

    expect(component.conflict()).toContain('reload');
  });

  it('should show generic error when update fails for other reasons (negative)', () => {
    employeeService.updateSalary.and.returnValue(throwError(() => new Error('boom')));
    const component = fixture.componentInstance;
    component.salaryForm.patchValue({
      newSalary: 170000,
      reason: 'Annual merit performance promotion'
    });
    component.onSalarySubmit();
    fixture.detectChanges();

    expect(component.salaryError()).toContain('Failed');
  });
});
