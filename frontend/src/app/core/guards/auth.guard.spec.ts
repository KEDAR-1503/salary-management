import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  it('should allow activation when session exists', (done) => {
    const auth = { checkSession: () => of({ username: 'hr_manager' }) } as AuthService;
    const router = { createUrlTree: jasmine.createSpy('createUrlTree') } as unknown as Router;

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router }
      ]
    });

    TestBed.runInInjectionContext(() => {
      (authGuard({} as never, {} as never) as Observable<boolean | UrlTree>).subscribe(result => {
        expect(result).toBe(true);
        expect(router.createUrlTree).not.toHaveBeenCalled();
        done();
      });
    });
  });

  it('should redirect to login when session is missing', (done) => {
    const loginTree = {} as UrlTree;
    const auth = { checkSession: () => of(null) } as AuthService;
    const router = {
      createUrlTree: jasmine.createSpy('createUrlTree').and.returnValue(loginTree)
    } as unknown as Router;

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router }
      ]
    });

    TestBed.runInInjectionContext(() => {
      (authGuard({} as never, {} as never) as Observable<boolean | UrlTree>).subscribe(result => {
        expect(result).toBe(loginTree);
        expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
        done();
      });
    });
  });
});
