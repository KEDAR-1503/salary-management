import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { AppComponent } from './app.component';

@Component({ standalone: true, template: '' })
class StubPageComponent {}

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent, StubPageComponent],
      providers: [
        provideRouter([
          { path: 'directory', component: StubPageComponent },
          { path: 'analytics', component: StubPageComponent },
          { path: 'login', component: StubPageComponent }
        ]),
        provideHttpClient()
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should hide Directory link on directory route and show Analytics', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/directory');
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Analytics');
    expect(text).not.toMatch(/\bDirectory\b/);
    expect(text).toContain('Logout');
  });

  it('should hide Analytics link on analytics route and show Directory', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/analytics');
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Directory');
    expect(text).not.toMatch(/\bAnalytics\b/);
  });

  it('should hide nav on login route', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/login');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('nav')).toBeNull();
  });
});
