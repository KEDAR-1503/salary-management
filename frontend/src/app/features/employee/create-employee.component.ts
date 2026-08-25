import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';

@Component({
  selector: 'app-create-employee',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section>
      <h2>Create Employee</h2>
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <label>Employee ID<input formControlName="employeeIdentifier" /></label>
        <label>Full Name<input formControlName="fullName" /></label>
        <label>Email<input type="email" formControlName="email" /></label>
        <label>Department<input formControlName="department" /></label>
        <label>Role Title<input formControlName="roleTitle" /></label>
        <label>Country<input formControlName="country" /></label>
        <label>Currency<input formControlName="currency" maxlength="3" /></label>
        <label>Initial Salary<input type="number" formControlName="initialSalary" /></label>
        <label>Effective Date<input type="date" formControlName="effectiveDate" /></label>
        @if (error()) { <p class="error">{{ error() }}</p> }
        <div class="actions">
          <button type="submit" [disabled]="form.invalid || saving()">Create</button>
          <a routerLink="/directory">Cancel</a>
        </div>
      </form>
    </section>
  `,
  styles: [`
    form { max-width: 480px; display: flex; flex-direction: column; gap: 0.75rem; }
    label { display: flex; flex-direction: column; }
    input { padding: 0.5rem; margin-top: 0.25rem; }
    .actions { display: flex; gap: 1rem; align-items: center; margin-top: 1rem; }
    button { padding: 0.5rem 1rem; background: #1976d2; color: white; border: none; border-radius: 4px; }
    .error { color: #c62828; }
  `]
})
export class CreateEmployeeComponent {
  private readonly employeeService = inject(EmployeeService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    employeeIdentifier: ['', Validators.required],
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    department: ['', Validators.required],
    roleTitle: ['', Validators.required],
    country: ['', Validators.required],
    currency: ['USD', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    initialSalary: [0, [Validators.required, Validators.min(0.01)]],
    effectiveDate: [new Date().toISOString().slice(0, 10), Validators.required]
  });

  onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set(null);
    this.employeeService.createEmployee(this.form.getRawValue()).subscribe({
      next: emp => this.router.navigate(['/employees', emp.id]),
      error: () => {
        this.error.set('Failed to create employee.');
        this.saving.set(false);
      }
    });
  }
}
