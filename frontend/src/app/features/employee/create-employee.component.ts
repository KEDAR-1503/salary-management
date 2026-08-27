import { Component, ChangeDetectionStrategy, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';

const CURRENCY_BY_COUNTRY: Record<string, string> = {
  'United States': 'USD',
  'United Kingdom': 'GBP',
  Germany: 'EUR',
  India: 'INR',
  Singapore: 'SGD'
};

@Component({
  selector: 'app-create-employee',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section>
      <h2>Create Employee</h2>
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <label>Full Name<input formControlName="fullName" /></label>
        <label>Email<input type="email" formControlName="email" /></label>
        <label>Department
          <select formControlName="department">
            <option value="" disabled>Select department</option>
            @for (dept of departments(); track dept) {
              <option [value]="dept">{{ dept }}</option>
            }
          </select>
        </label>
        <label>Role Title
          <select formControlName="roleTitle">
            <option value="" disabled>Select role</option>
            @for (role of roleTitles(); track role) {
              <option [value]="role">{{ role }}</option>
            }
          </select>
        </label>
        <label>Country
          <select formControlName="country" (change)="onCountryChange(form.controls.country.value)">
            <option value="" disabled>Select country</option>
            @for (country of countries(); track country) {
              <option [value]="country">{{ country }}</option>
            }
          </select>
        </label>
        <label>Currency
          <select formControlName="currency">
            @for (code of currencies; track code) {
              <option [value]="code">{{ code }}</option>
            }
          </select>
        </label>
        <label>Initial Salary (in {{ form.controls.currency.value }})
          <input type="number" formControlName="initialSalary" />
        </label>
        <label>Effective Date
          <input type="date" formControlName="effectiveDate" [attr.min]="today" />
        </label>
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
    input, select { padding: 0.5rem; margin-top: 0.25rem; }
    select:disabled { background: #f5f5f5; color: #555; }
    .actions { display: flex; gap: 1rem; align-items: center; margin-top: 1rem; }
    button { padding: 0.5rem 1rem; background: #1976d2; color: white; border: none; border-radius: 4px; }
    button:disabled { opacity: 0.6; }
    .error { color: #c62828; }
  `]
})
export class CreateEmployeeComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly departments = signal<string[]>([]);
  readonly countries = signal<string[]>([]);
  readonly roleTitles = signal<string[]>([]);
  readonly currencies = Object.values(CURRENCY_BY_COUNTRY);
  readonly today = new Date().toISOString().slice(0, 10);

  private readonly notPastDateValidator = (control: { value: string }) => {
    if (!control.value) return null;
    return control.value < this.today ? { pastDate: true } : null;
  };

  readonly form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    department: ['', Validators.required],
    roleTitle: ['', Validators.required],
    country: ['', Validators.required],
    currency: [{ value: 'USD', disabled: true }, [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    initialSalary: [0, [Validators.required, Validators.min(0.01)]],
    effectiveDate: [this.today, [Validators.required, this.notPastDateValidator]]
  });

  ngOnInit(): void {
    this.employeeService.getFilterOptions().subscribe({
      next: options => {
        this.departments.set(options.departments);
        this.countries.set(options.countries);
        this.roleTitles.set(options.roleTitles);
      },
      error: () => this.error.set('Failed to load organisation catalog.')
    });
  }

  onCountryChange(country: string): void {
    const currency = CURRENCY_BY_COUNTRY[country];
    if (currency) {
      this.form.controls.currency.setValue(currency);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set(null);
    const raw = this.form.getRawValue();
    this.employeeService.createEmployee({
      fullName: raw.fullName,
      email: raw.email,
      department: raw.department,
      roleTitle: raw.roleTitle,
      country: raw.country,
      currency: raw.currency,
      initialSalary: raw.initialSalary,
      effectiveDate: raw.effectiveDate
    }).subscribe({
      next: emp => this.router.navigate(['/employees', emp.id]),
      error: () => {
        this.error.set('Failed to create employee.');
        this.saving.set(false);
      }
    });
  }
}
