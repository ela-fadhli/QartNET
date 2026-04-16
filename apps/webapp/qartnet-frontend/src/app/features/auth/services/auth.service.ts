import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { LoginRequest, RegisterRequest, AuthResponse, ApiResponse } from '../models/auth.models';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/auth`;

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.baseUrl}/login`, request)
      .pipe(map((res) => res.data!));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.baseUrl}/register`, request)
      .pipe(map((res) => res.data!));
  }

  saveToken(token: string): void {
    localStorage.setItem('qartnet_token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('qartnet_token');
  }

  logout(): void {
    localStorage.removeItem('qartnet_token');
  }
}
