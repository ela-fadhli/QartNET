import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserProfile, UpdateProfileRequest } from '../models/profile.models';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/users`;

  getMyProfile(): Observable<UserProfile> {
    return this.http
      .get<ApiResponse<UserProfile>>(`${this.baseUrl}/me`)
      .pipe(map((res) => res.data!));
  }

  updateMyProfile(req: UpdateProfileRequest): Observable<UserProfile> {
    return this.http
      .put<ApiResponse<UserProfile>>(`${this.baseUrl}/me`, req)
      .pipe(map((res) => res.data!));
  }

  getPublicProfile(publicId: string): Observable<any> {
    return this.http
      .get<ApiResponse<any>>(`${this.baseUrl}/${publicId}`)
      .pipe(map((res) => res.data!));
  }
}
