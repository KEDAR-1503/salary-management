import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login.component';
import { EmployeeDirectoryComponent } from './features/directory/employee-directory.component';
import { CreateEmployeeComponent } from './features/employee/create-employee.component';
import { EmployeeDetailComponent } from './features/employee/employee-detail.component';
import { AnalyticsDashboardComponent } from './features/analytics/analytics-dashboard.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: 'directory', pathMatch: 'full' },
  { path: 'directory', component: EmployeeDirectoryComponent, canActivate: [authGuard] },
  { path: 'employees/new', component: CreateEmployeeComponent, canActivate: [authGuard] },
  { path: 'employees/:id', component: EmployeeDetailComponent, canActivate: [authGuard] },
  { path: 'analytics', component: AnalyticsDashboardComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'directory' }
];
