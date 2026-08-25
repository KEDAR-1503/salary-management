import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface AuthUser {
  username: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/auth';

  login(username: string, password: string): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.baseUrl}/login`, { username, password });
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {});
  }

  me(): Observable<AuthUser> {
    return this.http.get<AuthUser>(`${this.baseUrl}/me`);
  }

  checkSession(): Observable<AuthUser | null> {
    return new Observable(subscriber => {
      this.me().subscribe({
        next: user => { subscriber.next(user); subscriber.complete(); },
        error: () => { subscriber.next(null); subscriber.complete(); }
      });
    });
  }
}
