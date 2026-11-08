import { TestBed, fakeAsync, flushMicrotasks } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService, csrfInterceptor } from './auth.service';
describe('AuthService', () => {
  let http: HttpTestingController;
  let auth: AuthService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([csrfInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });
  afterEach(() => http.verify());
  it('registers without hiding a second automatic sign-in operation', fakeAsync(() => {
    const operation = auth.register({
      nom: 'Last',
      prenom: 'First',
      email: 'new@example.com',
      password: 'Valid-password-123',
    });
    http
      .expectOne('/api/auth/csrf')
      .flush({ headerName: 'X-CSRF-TOKEN', token: 'registration-token' });
    flushMicrotasks();
    const registration = http.expectOne('/api/auth/register');
    expect(registration.request.headers.get('X-CSRF-TOKEN')).toBe('registration-token');
    registration.flush({ id: '1', role: 'USER' });
    let completed = false;
    void operation.then(() => {
      completed = true;
    });
    flushMicrotasks();
    expect(completed).toBeTrue();
    http.expectNone('/api/auth/login');
  }));
  it('refreshes CSRF after login and propagates a restoration outage', fakeAsync(() => {
    const login = auth.login('user@example.com', 'Valid-password-123');
    http.expectOne('/api/auth/csrf').flush({ headerName: 'X-CSRF-TOKEN', token: 'before-login' });
    flushMicrotasks();
    const request = http.expectOne('/api/auth/login');
    expect(request.request.headers.get('X-CSRF-TOKEN')).toBe('before-login');
    request.flush(null, { status: 204, statusText: 'No Content' });
    flushMicrotasks();
    http
      .expectOne('/api/auth/me')
      .flush({ id: '1', nom: 'Last', prenom: 'First', email: 'user@example.com', role: 'USER' });
    flushMicrotasks();
    http.expectOne('/api/auth/csrf').flush({ headerName: 'X-CSRF-TOKEN', token: 'after-login' });
    let id = '';
    void login.then((user) => {
      id = user.id;
    });
    flushMicrotasks();
    expect(id).toBe('1');
    const restore = auth.restore();
    http.expectOne('/api/auth/me').flush('offline', { status: 503, statusText: 'Unavailable' });
    let failed = false;
    void restore.catch(() => {
      failed = true;
    });
    flushMicrotasks();
    expect(failed).toBeTrue();
    expect(auth.user()).toBeNull();
  }));
});
