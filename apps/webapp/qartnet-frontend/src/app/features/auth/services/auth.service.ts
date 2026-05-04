import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, throwError, EMPTY } from 'rxjs';
import { map, tap, catchError, switchMap, filter, take } from 'rxjs/operators';
import {
  LoginRequest, RegisterRequest, AuthResponse,
  RefreshTokenRequest, ForgotPasswordRequest, ResetPasswordRequest
} from '../models/auth.models';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { TokenService } from '../../../core/services/token.service';
import { environment } from '../../../../environments/environment';
import { WebSocketService } from '../../../core/services/websocket.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private tokenService = inject(TokenService);
  private wsService = inject(WebSocketService);
  private baseUrl = `${environment.apiUrl}/api/auth`;

  private isRefreshing = false;
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.baseUrl}/login`, request)
      .pipe(
        map((res) => res.data!),
        tap((data) => {
          this.tokenService.saveTokens(data.accessToken, data.refreshToken);
          this.wsService.connect(data.accessToken);
        }),
      );
  }

  register(request: RegisterRequest): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/register`, request)
      .pipe(map(() => void 0));
  }

  verifyEmail(token: string): Observable<void> {
    return this.http
      .get<ApiResponse<void>>(`${this.baseUrl}/verify-email`, { params: { token } })
      .pipe(map(() => void 0));
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/forgot-password`, request)
      .pipe(map(() => void 0));
  }

  resetPassword(request: ResetPasswordRequest): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/reset-password`, request)
      .pipe(map(() => void 0));
  }

  refreshAccessToken(): Observable<string> {
    const refreshToken = this.tokenService.getRefreshToken();
    if (!refreshToken) {
      this.clearAndRedirect();
      return EMPTY;
    }
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.baseUrl}/refresh`, { refreshToken } as RefreshTokenRequest)
      .pipe(
        map((res) => res.data!),
        tap((data) => this.tokenService.saveTokens(data.accessToken, data.refreshToken)),
        map((data) => data.accessToken),
        catchError((err) => { this.clearAndRedirect(); return throwError(() => err); }),
      );
  }

  handleTokenRefresh(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
    if (this.isRefreshing) {
      return this.refreshTokenSubject.pipe(
        filter((token): token is string => token !== null),
        take(1),
        switchMap((token) =>
          next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })),
        ),
      );
    }
    this.isRefreshing = true;
    this.refreshTokenSubject.next(null);

    return this.refreshAccessToken().pipe(
      switchMap((newToken) => {
        this.isRefreshing = false;
        this.refreshTokenSubject.next(newToken);
        return next(req.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } }));
      }),
      catchError((err) => { this.isRefreshing = false; return throwError(() => err); }),
    );
  }

  logout(): void {
    const refreshToken = this.tokenService.getRefreshToken();
    if (refreshToken) {
      this.http
        .post<ApiResponse<void>>(`${this.baseUrl}/logout`, { refreshToken })
        .pipe(catchError(() => EMPTY))
        .subscribe();
    }
    this.wsService.disconnect();
    this.clearAndRedirect();
  }

  isAuthenticated(): boolean {
    return this.tokenService.isAccessTokenValid();
  }

  hasRole(role: string): boolean {
    return this.tokenService.hasRole(role);
  }

  getAccessToken(): string | null {
    return this.tokenService.getAccessToken();
  }

  /** @deprecated use getAccessToken() */
  getToken(): string | null {
    return this.getAccessToken();
  }

  private clearAndRedirect(): void {
    this.tokenService.clearTokens();
    this.router.navigate(['/auth/sign-in']);
  }
}
