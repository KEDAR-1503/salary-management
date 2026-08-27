import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="login-card">
      <h1>ACME Salary Management</h1>
      <p>Sign in as HR Manager</p>
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <label>Username<input formControlName="username" autocomplete="username" /></label>
        <label>Password<input type="password" formControlName="password" autocomplete="current-password" /></label>
        @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
        <button type="submit" [disabled]="form.invalid || loading()">Sign in</button>
      </form>
    </div>
  `,
  styles: [`
    .login-card { max-width: 400px; margin: 4rem auto; padding: 2rem; border: 1px solid #ddd; border-radius: 8px; }
    label { display: block; margin-bottom: 1rem; }
    input { width: 100%; padding: 0.5rem; margin-top: 0.25rem; box-sizing: border-box; }
    button { width: 100%; padding: 0.75rem; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
    button:disabled { opacity: 0.6; cursor: not-allowed; }
    .error { color: #c62828; }
    @media (max-width: 640px) {
      .login-card { margin: 1.5rem 1rem; padding: 1.25rem; }
    }
  `]
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set(null);
    const { username, password } = this.form.getRawValue();
    this.auth.login(username, password).subscribe({
      next: () => this.router.navigate(['/directory']),
      error: () => {
        this.error.set('Invalid credentials. Please try again.');
        this.loading.set(false);
      }
    });
  }
}
