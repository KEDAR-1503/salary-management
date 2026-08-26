import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../core/services/auth.service';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let auth: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    auth = jasmine.createSpyObj('AuthService', ['login']);
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
  });

  it('should disable submit when form is empty', () => {
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(button.disabled).toBeTrue();
  });

  it('should navigate to directory on successful login', fakeAsync(() => {
    auth.login.and.returnValue(of({ username: 'hr_manager' }));
    const component = fixture.componentInstance;
    component.form.setValue({ username: 'hr_manager', password: 'admin123' });
    component.onSubmit();
    tick();

    expect(auth.login).toHaveBeenCalledWith('hr_manager', 'admin123');
    expect(router.navigate).toHaveBeenCalledWith(['/directory']);
  }));

  it('should show error message on failed login', fakeAsync(() => {
    auth.login.and.returnValue(throwError(() => new Error('unauthorized')));
    const component = fixture.componentInstance;
    component.form.setValue({ username: 'hr_manager', password: 'wrong' });
    component.onSubmit();
    tick();
    fixture.detectChanges();

    expect(component.error()).toBe('Invalid credentials. Please try again.');
    const alert = fixture.nativeElement.querySelector('[role="alert"]');
    expect(alert.textContent).toContain('Invalid credentials');
  }));
});
