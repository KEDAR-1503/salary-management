import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DepartmentCompensationSummary, CountryCompensationSummary } from '../models/analytics.model';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/analytics';

  getDepartmentSummaries(): Observable<DepartmentCompensationSummary[]> {
    return this.http.get<DepartmentCompensationSummary[]>(`${this.baseUrl}/departments`);
  }

  getCountrySummaries(): Observable<CountryCompensationSummary[]> {
    return this.http.get<CountryCompensationSummary[]>(`${this.baseUrl}/countries`);
  }
}
