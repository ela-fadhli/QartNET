import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ProfileResponse, UpdateProfileRequest } from '../models/profile.models';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/profile`;

  getMyProfile(): Observable<ProfileResponse> {
    return this.http
      .get<ApiResponse<ProfileResponse>>(`${this.baseUrl}/me`)
      .pipe(map((res) => res.data!));
  }

  updateMyProfile(request: UpdateProfileRequest): Observable<ProfileResponse> {
    return this.http
      .put<ApiResponse<ProfileResponse>>(`${this.baseUrl}/me`, request)
      .pipe(map((res) => res.data!));
  }

  getPublicProfile(username: string): Observable<ProfileResponse> {
    return this.http
      .get<ApiResponse<ProfileResponse>>(`${this.baseUrl}/${username}`)
      .pipe(map((res) => res.data!));
  }
}
