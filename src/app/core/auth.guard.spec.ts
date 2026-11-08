import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, UrlTree } from '@angular/router';
import type { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';
import { authGuard, adminGuard } from './auth.guard';
describe('Authentication guard failures', () => {
  it('separates an unauthenticated session from infrastructure failure', async () => {
    const restore = jasmine.createSpy('restore').and.resolveTo(null);
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: { restore } }],
    });
    const run = () =>
      TestBed.runInInjectionContext(() =>
        authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
      );
    const anonymous = await run();
    expect(anonymous instanceof UrlTree && TestBed.inject(Router).serializeUrl(anonymous)).toBe(
      '/login',
    );
    restore.and.rejectWith(new Error('offline'));
    const outage = await run();
    expect(outage instanceof UrlTree && TestBed.inject(Router).serializeUrl(outage)).toBe(
      '/login?unavailable=true',
    );
  });
  it('redirects a customer to accounts rather than administrator content', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { restore: () => Promise.resolve({ role: 'USER' }) } },
      ],
    });
    const result = await TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
    expect(result instanceof UrlTree && TestBed.inject(Router).serializeUrl(result)).toBe('/home');
  });
});
