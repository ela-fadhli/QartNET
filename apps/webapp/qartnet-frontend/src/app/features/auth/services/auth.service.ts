import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { LoginRequest, RegisterRequest, AuthResponse, ApiResponse, ForgotPasswordRequest, ResetPasswordRequest } from '../models/auth.models';
import { environment } from '../../../../environments/environment';
import { WebSocketService } from '../../../core/services/websocket.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private wsService = inject(WebSocketService);
  private baseUrl = `${environment.apiUrl}/api/auth`;

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.baseUrl}/login`, request).pipe(
      map((res) => {
        const data = res.data!;
        this.saveToken(data.token);
        this.wsService.connect(data.token);
        return data;
      }),
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.baseUrl}/register`, request)
      .pipe(map((res) => res.data!));
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

  saveToken(token: string): void {
    localStorage.setItem('qartnet_token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('qartnet_token');
  }

  logout(): void {
    this.wsService.disconnect();
    localStorage.removeItem('qartnet_token');
  }
}
