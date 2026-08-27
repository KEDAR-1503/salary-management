import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AnalyticsDashboardComponent } from './analytics-dashboard.component';
import { AnalyticsService } from '../../core/services/analytics.service';

describe('AnalyticsDashboardComponent', () => {
  let fixture: ComponentFixture<AnalyticsDashboardComponent>;
  let analyticsService: jasmine.SpyObj<AnalyticsService>;

  beforeEach(async () => {
    analyticsService = jasmine.createSpyObj('AnalyticsService', [
      'getDepartmentSummaries',
      'getCountrySummaries'
    ]);
    analyticsService.getDepartmentSummaries.and.returnValue(of([{
      department: 'Engineering',
      currency: 'USD',
      headcount: 10,
      averageSalary: 150000,
      medianSalary: 140000
    }]));
    analyticsService.getCountrySummaries.and.returnValue(of([{
      country: 'United States',
      currency: 'USD',
      headcount: 20,
      averageSalary: 130000,
      medianSalary: 125000
    }]));

    await TestBed.configureTestingModule({
      imports: [AnalyticsDashboardComponent],
      providers: [{ provide: AnalyticsService, useValue: analyticsService }]
    }).compileComponents();

    fixture = TestBed.createComponent(AnalyticsDashboardComponent);
    fixture.detectChanges();
  });

  it('should render currency-separated department and country summaries', () => {
    expect(analyticsService.getDepartmentSummaries).toHaveBeenCalled();
    expect(analyticsService.getCountrySummaries).toHaveBeenCalled();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Engineering (USD)');
    expect(text).toContain('United States (USD)');
    expect(text).toContain('never combined across currencies');
  });

  it('should render no metric cards when analytics APIs return empty lists', () => {
    analyticsService.getDepartmentSummaries.and.returnValue(of([]));
    analyticsService.getCountrySummaries.and.returnValue(of([]));
    fixture = TestBed.createComponent(AnalyticsDashboardComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.departments()).toEqual([]);
    expect(fixture.componentInstance.countries()).toEqual([]);
    expect(fixture.nativeElement.querySelectorAll('.card').length).toBe(0);
  });
});
