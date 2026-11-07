import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
export const authGuard: CanActivateFn = async () => {
  const router = inject(Router),
    auth = inject(AuthService);
  try {
    return (await auth.restore()) ? true : router.createUrlTree(['/login']);
  } catch {
    return router.createUrlTree(['/login']);
  }
};
export const adminGuard: CanActivateFn = async () => {
  const router = inject(Router),
    auth = inject(AuthService);
  try {
    const user = await auth.restore();
    return user?.role === 'ADMIN' ? true : router.createUrlTree([user ? '/home' : '/login']);
  } catch {
    return router.createUrlTree(['/login']);
  }
};
