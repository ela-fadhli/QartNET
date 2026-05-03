import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../features/auth/services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated() && authService.hasRole('ADMIN')) return true;

  if (!authService.isAuthenticated()) return router.createUrlTree(['/auth/sign-in']);

  return router.createUrlTree(['/profile']);
};
