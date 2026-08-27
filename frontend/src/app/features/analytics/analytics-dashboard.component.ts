import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AnalyticsService } from '../../core/services/analytics.service';
import { DepartmentCompensationSummary, CountryCompensationSummary } from '../../core/models/analytics.model';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section>
      <h2>Compensation Analytics</h2>
      <p class="note">Metrics are grouped by currency — never combined across currencies.</p>

      <h3>By Department</h3>
      <div class="cards">
        @for (row of departments(); track row.department + row.currency) {
          <article class="card">
            <h4>{{ row.department }} ({{ row.currency }})</h4>
            <p>Headcount: {{ row.headcount }}</p>
            <p>Average: {{ row.averageSalary | number:'1.2-2' }} {{ row.currency }}</p>
            <p>Median: {{ row.medianSalary | number:'1.2-2' }} {{ row.currency }}</p>
          </article>
        }
      </div>

      <h3>By Country</h3>
      <div class="cards">
        @for (row of countries(); track row.country + row.currency) {
          <article class="card">
            <h4>{{ row.country }} ({{ row.currency }})</h4>
            <p>Headcount: {{ row.headcount }}</p>
            <p>Average: {{ row.averageSalary | number:'1.2-2' }} {{ row.currency }}</p>
            <p>Median: {{ row.medianSalary | number:'1.2-2' }} {{ row.currency }}</p>
          </article>
        }
      </div>
    </section>
  `,
  styles: [`
    .note { color: #666; font-style: italic; }
    .cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 1rem; margin-bottom: 2rem; }
    .card { border: 1px solid #ddd; border-radius: 8px; padding: 1rem; }
    .card h4 { margin: 0 0 0.5rem; }
    .card p { margin: 0.25rem 0; }
    @media (max-width: 640px) {
      .cards { grid-template-columns: 1fr; }
    }
  `]
})
export class AnalyticsDashboardComponent implements OnInit {
  private readonly analyticsService = inject(AnalyticsService);

  readonly departments = signal<DepartmentCompensationSummary[]>([]);
  readonly countries = signal<CountryCompensationSummary[]>([]);

  ngOnInit(): void {
    this.analyticsService.getDepartmentSummaries().subscribe(d => this.departments.set(d));
    this.analyticsService.getCountrySummaries().subscribe(c => this.countries.set(c));
  }
}
