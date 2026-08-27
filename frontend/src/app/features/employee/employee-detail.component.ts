import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EmployeeService, SalaryConflictError } from '../../core/services/employee.service';
import { Employee, SalaryAuditLog } from '../../core/models/employee.model';

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (employee(); as emp) {
      <section>
        <a routerLink="/directory">&larr; Back to directory</a>
        <h2>{{ emp.fullName }}</h2>
        <dl class="detail-grid">
          <dt>Employee ID</dt><dd>{{ emp.employeeIdentifier }}</dd>
          <dt>Email</dt><dd>{{ emp.email }}</dd>
          <dt>Department</dt><dd>{{ emp.department }}</dd>
          <dt>Country</dt><dd>{{ emp.country }}</dd>
          <dt>Current Salary</dt><dd>{{ emp.currentSalary | number:'1.2-2' }} {{ emp.currency }}</dd>
          <dt>Effective Date</dt><dd>{{ emp.effectiveDate }}</dd>
        </dl>

        <h3>Change Salary</h3>
        <form [formGroup]="salaryForm" (ngSubmit)="onSalarySubmit()">
          <label>New Salary (in {{ emp.currency }})
            <input type="number" formControlName="newSalary" />
          </label>
          <label>Effective Date
            <input type="date" formControlName="effectiveDate" [attr.min]="today" />
          </label>
          <label>Reason (min 10 chars)<textarea formControlName="reason" rows="3"></textarea></label>
          @if (conflict()) { <p class="conflict" role="alert">{{ conflict() }} <button type="button" (click)="reload()">Reload</button></p> }
          @if (salaryError()) { <p class="error">{{ salaryError() }}</p> }
          <button type="submit" [disabled]="salaryForm.invalid || saving()">Update Salary</button>
        </form>

        <h3>Salary History</h3>
        <table>
          <thead><tr><th>When</th><th>Previous</th><th>New</th><th>Reason</th><th>By</th></tr></thead>
          <tbody>
            @for (log of history(); track log.id) {
              <tr>
                <td>{{ log.changedAt | date:'medium' }}</td>
                <td>{{ log.previousSalary != null ? (log.previousSalary | number:'1.2-2') + ' ' + log.currency : '—' }}</td>
                <td>{{ log.newSalary | number:'1.2-2' }} {{ log.currency }}</td>
                <td>{{ log.reason }}</td>
                <td>{{ log.changedBy }}</td>
              </tr>
            }
          </tbody>
        </table>
      </section>
    } @else if (loading()) {
      <p>Loading...</p>
    }
  `,
  styles: [`
    .detail-grid { display: grid; grid-template-columns: 160px 1fr; gap: 0.5rem; margin: 1rem 0; }
    form { max-width: 480px; display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 2rem; }
    label { display: flex; flex-direction: column; }
    input, textarea { padding: 0.5rem; margin-top: 0.25rem; }
    table { width: 100%; border-collapse: collapse; }
    th, td { border-bottom: 1px solid #eee; padding: 0.5rem; text-align: left; }
    .conflict { background: #fff3e0; padding: 0.75rem; border-radius: 4px; color: #e65100; }
    .error { color: #c62828; }
    button { padding: 0.5rem 1rem; background: #1976d2; color: white; border: none; border-radius: 4px; width: fit-content; }
  `]
})
export class EmployeeDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly employeeService = inject(EmployeeService);
  private readonly fb = inject(FormBuilder);

  readonly employee = signal<Employee | null>(null);
  readonly history = signal<SalaryAuditLog[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly conflict = signal<string | null>(null);
  readonly salaryError = signal<string | null>(null);
  readonly today = new Date().toISOString().slice(0, 10);

  private readonly notPastDateValidator = (control: { value: string }) => {
    if (!control.value) return null;
    return control.value < this.today ? { pastDate: true } : null;
  };

  readonly salaryForm = this.fb.nonNullable.group({
    newSalary: [0, [Validators.required, Validators.min(0.01)]],
    effectiveDate: [this.today, [Validators.required, this.notPastDateValidator]],
    reason: ['', [Validators.required, Validators.minLength(10)]]
  });

  private employeeId = 0;

  ngOnInit(): void {
    this.employeeId = Number(this.route.snapshot.paramMap.get('id'));
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.conflict.set(null);
    this.employeeService.getEmployee(this.employeeId).subscribe({
      next: emp => {
        this.employee.set(emp);
        this.salaryForm.patchValue({
          newSalary: emp.currentSalary,
          effectiveDate: this.today
        });
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
    this.employeeService.getHistory(this.employeeId).subscribe({
      next: logs => this.history.set(logs)
    });
  }

  onSalarySubmit(): void {
    const emp = this.employee();
    if (!emp || this.salaryForm.invalid) return;
    this.saving.set(true);
    this.conflict.set(null);
    this.salaryError.set(null);
    const { newSalary, effectiveDate, reason } = this.salaryForm.getRawValue();
    this.employeeService.updateSalary(this.employeeId, {
      version: emp.version,
      newSalary,
      effectiveDate,
      reason
    }).subscribe({
      next: updated => {
        this.employee.set(updated);
        this.saving.set(false);
        this.employeeService.getHistory(this.employeeId).subscribe(logs => this.history.set(logs));
      },
      error: err => {
        this.saving.set(false);
        if (err instanceof SalaryConflictError) {
          this.conflict.set(err.message);
        } else {
          this.salaryError.set('Failed to update salary.');
        }
      }
    });
  }
}
