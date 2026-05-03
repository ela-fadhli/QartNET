import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../../features/auth/services/auth.service';

const AUTH_PATHS = ['/api/auth/login', '/api/auth/register', '/api/auth/forgot-password',
                    '/api/auth/reset-password', '/api/auth/verify-email', '/api/auth/refresh'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  const isPublicAuthPath = AUTH_PATHS.some((p) => req.url.includes(p));
  const token = authService.getAccessToken();

  const authReq =
    token && !isPublicAuthPath
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isPublicAuthPath && token) {
        return authService.handleTokenRefresh(req, next);
      }
      return throwError(() => error);
    }),
  );
};
