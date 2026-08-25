import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';
import { Employee } from '../../core/models/employee.model';

@Component({
  selector: 'app-employee-directory',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section>
      <header class="toolbar">
        <h2>Employee Directory</h2>
        <a routerLink="/employees/new" class="btn">Create Employee</a>
      </header>
      <div class="filters">
        <input [(ngModel)]="search" placeholder="Search by name or ID" (keyup.enter)="load()" />
        <input [(ngModel)]="department" placeholder="Department" (keyup.enter)="load()" />
        <input [(ngModel)]="country" placeholder="Country" (keyup.enter)="load()" />
        <button (click)="load()">Search</button>
      </div>
      @if (loading()) { <p>Loading...</p> }
      @if (error()) { <p class="error">{{ error() }}</p> }
      <table>
        <thead>
          <tr>
            <th>ID</th><th>Name</th><th>Department</th><th>Country</th>
            <th>Salary</th><th>Currency</th><th></th>
          </tr>
        </thead>
        <tbody>
          @for (emp of employees(); track emp.id) {
            <tr>
              <td>{{ emp.employeeIdentifier }}</td>
              <td>{{ emp.fullName }}</td>
              <td>{{ emp.department }}</td>
              <td>{{ emp.country }}</td>
              <td>{{ emp.currentSalary | number:'1.2-2' }}</td>
              <td>{{ emp.currency }}</td>
              <td><a [routerLink]="['/employees', emp.id]">View</a></td>
            </tr>
          }
        </tbody>
      </table>
      <div class="pagination">
        <button [disabled]="page() === 0" (click)="prevPage()">Previous</button>
        <span>Page {{ page() + 1 }} of {{ totalPages() }}</span>
        <button [disabled]="page() + 1 >= totalPages()" (click)="nextPage()">Next</button>
      </div>
    </section>
  `,
  styles: [`
    .toolbar { display: flex; justify-content: space-between; align-items: center; }
    .filters { display: flex; gap: 0.5rem; margin: 1rem 0; flex-wrap: wrap; }
    .filters input { padding: 0.5rem; }
    table { width: 100%; border-collapse: collapse; }
    th, td { border-bottom: 1px solid #eee; padding: 0.5rem; text-align: left; }
    .pagination { margin-top: 1rem; display: flex; gap: 1rem; align-items: center; }
    .btn { padding: 0.5rem 1rem; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; }
    .error { color: #c62828; }
  `]
})
export class EmployeeDirectoryComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);

  readonly employees = signal<Employee[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal(0);
  readonly totalPages = signal(1);

  search = '';
  department = '';
  country = '';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.employeeService.getEmployees({
      page: this.page(),
      size: 20,
      search: this.search || undefined,
      department: this.department || undefined,
      country: this.country || undefined
    }).subscribe({
      next: res => {
        this.employees.set(res.content);
        this.totalPages.set(res.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load employees.');
        this.loading.set(false);
      }
    });
  }

  prevPage(): void { this.page.update(p => p - 1); this.load(); }
  nextPage(): void { this.page.update(p => p + 1); this.load(); }
}
