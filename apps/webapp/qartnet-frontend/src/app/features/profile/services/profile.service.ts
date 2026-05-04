import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserProfile, ProfileResponse, UpdateProfileRequest } from '../models/profile.models';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private http = inject(HttpClient);
  private userBaseUrl = `${environment.apiUrl}/api/users`;
  private profileBaseUrl = `${environment.apiUrl}/api/profile`;

  /** Full user profile with roles, skills, socialLinks — used by the admin-backoffice profile page. */
  getMyUserProfile(): Observable<UserProfile> {
    return this.http
      .get<ApiResponse<UserProfile>>(`${this.userBaseUrl}/me`)
      .pipe(map((res) => res.data!));
  }

  updateMyUserProfile(req: UpdateProfileRequest): Observable<UserProfile> {
    return this.http
      .put<ApiResponse<UserProfile>>(`${this.userBaseUrl}/me`, req)
      .pipe(map((res) => res.data!));
  }

  /** Slim profile for forum/social features. */
  getMyProfile(): Observable<ProfileResponse> {
    return this.http
      .get<ApiResponse<ProfileResponse>>(`${this.profileBaseUrl}/me`)
      .pipe(map((res) => res.data!));
  }

  updateMyProfile(request: UpdateProfileRequest): Observable<ProfileResponse> {
    return this.http
      .put<ApiResponse<ProfileResponse>>(`${this.profileBaseUrl}/me`, request)
      .pipe(map((res) => res.data!));
  }

  getPublicProfile(username: string): Observable<ProfileResponse> {
    return this.http
      .get<ApiResponse<ProfileResponse>>(`${this.profileBaseUrl}/${username}`)
      .pipe(map((res) => res.data!));
  }

  getPublicUserProfile(publicId: string): Observable<any> {
    return this.http
      .get<ApiResponse<any>>(`${this.userBaseUrl}/${publicId}`)
      .pipe(map((res) => res.data!));
  }
}
