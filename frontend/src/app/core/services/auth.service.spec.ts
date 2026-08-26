import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should POST login credentials', () => {
    service.login('hr_manager', 'admin123').subscribe(user => {
      expect(user.username).toBe('hr_manager');
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'hr_manager', password: 'admin123' });
    req.flush({ username: 'hr_manager' });
  });

  it('should POST logout', () => {
    service.logout().subscribe();
    const req = httpMock.expectOne('/api/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });

  it('should GET current session user', () => {
    service.me().subscribe(user => {
      expect(user.username).toBe('hr_manager');
    });

    const req = httpMock.expectOne('/api/auth/me');
    expect(req.request.method).toBe('GET');
    req.flush({ username: 'hr_manager' });
  });

  it('should return null from checkSession when /me fails', (done) => {
    service.checkSession().subscribe(user => {
      expect(user).toBeNull();
      done();
    });

    const req = httpMock.expectOne('/api/auth/me');
    req.flush(null, { status: 401, statusText: 'Unauthorized' });
  });

  it('should return user from checkSession when /me succeeds', (done) => {
    service.checkSession().subscribe(user => {
      expect(user?.username).toBe('hr_manager');
      done();
    });

    const req = httpMock.expectOne('/api/auth/me');
    req.flush({ username: 'hr_manager' });
  });
});
