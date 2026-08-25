import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterOutlet, NavigationEnd } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { filter, map, startWith } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  template: `
    @if (showNav()) {
      <nav class="nav">
        <a routerLink="/directory" class="brand">ACME Salary</a>
        <div class="links">
          <a routerLink="/directory">Directory</a>
          <a routerLink="/analytics">Analytics</a>
          <button type="button" (click)="logout()">Logout</button>
        </div>
      </nav>
    }
    <main [class.main]="showNav()"><router-outlet /></main>
  `,
  styles: [`
    .nav { display: flex; justify-content: space-between; align-items: center; padding: 0.75rem 1.5rem; background: #1976d2; color: white; }
    .brand { color: white; text-decoration: none; font-weight: bold; font-size: 1.1rem; }
    .links { display: flex; gap: 1rem; align-items: center; }
    .links a { color: white; text-decoration: none; }
    .links button { background: transparent; border: 1px solid white; color: white; padding: 0.25rem 0.75rem; border-radius: 4px; cursor: pointer; }
    .main { padding: 1.5rem; max-width: 1200px; margin: 0 auto; }
  `]
})
export class AppComponent {
  private readonly showNav$ = this.router.events.pipe(
    filter((e): e is NavigationEnd => e instanceof NavigationEnd),
    map(e => !e.urlAfterRedirects.startsWith('/login')),
    startWith(!this.router.url.startsWith('/login'))
  );
  readonly showNav = toSignal(this.showNav$, { initialValue: false });

  constructor(private auth: AuthService, private router: Router) {}

  logout(): void {
    this.auth.logout().subscribe({ complete: () => this.router.navigate(['/login']) });
  }
}
