import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AnalyticsService } from './analytics.service';
import { DepartmentCompensationSummary, CountryCompensationSummary } from '../models/analytics.model';

describe('AnalyticsService (TDD)', () => {
  let service: AnalyticsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AnalyticsService]
    });
    service = TestBed.inject(AnalyticsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch currency-separated department compensation summaries', () => {
    const mockData: DepartmentCompensationSummary[] = [
      {
        department: 'Engineering',
        currency: 'USD',
        headcount: 150,
        averageSalary: 145000.0,
        medianSalary: 140000.0
      },
      {
        department: 'Engineering',
        currency: 'EUR',
        headcount: 80,
        averageSalary: 85000.0,
        medianSalary: 82000.0
      }
    ];

    service.getDepartmentSummaries().subscribe(data => {
      expect(data.length).toBe(2);
      expect(data[0].department).toBe('Engineering');
      expect(data[0].currency).toBe('USD');
      expect(data[1].currency).toBe('EUR');
    });

    const req = httpMock.expectOne('/api/v1/analytics/departments');
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('should fetch currency-separated country compensation summaries', () => {
    const mockData: CountryCompensationSummary[] = [
      {
        country: 'United States',
        currency: 'USD',
        headcount: 500,
        averageSalary: 130000.0,
        medianSalary: 125000.0
      }
    ];

    service.getCountrySummaries().subscribe(data => {
      expect(data.length).toBe(1);
      expect(data[0].country).toBe('United States');
      expect(data[0].headcount).toBe(500);
    });

    const req = httpMock.expectOne('/api/v1/analytics/countries');
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('should surface department analytics HTTP failures (negative)', (done) => {
    service.getDepartmentSummaries().subscribe({
      next: () => done.fail('expected error'),
      error: err => {
        expect(err.status).toBe(500);
        done();
      }
    });

    const req = httpMock.expectOne('/api/v1/analytics/departments');
    req.flush({ detail: 'failure' }, { status: 500, statusText: 'Server Error' });
  });

  it('should surface country analytics HTTP failures (negative)', (done) => {
    service.getCountrySummaries().subscribe({
      next: () => done.fail('expected error'),
      error: err => {
        expect(err.status).toBe(503);
        done();
      }
    });

    const req = httpMock.expectOne('/api/v1/analytics/countries');
    req.flush({ detail: 'unavailable' }, { status: 503, statusText: 'Service Unavailable' });
  });
});
