import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/models/api-response.model';
import {
  DashboardStats, AdminUser, AdminReport, ActivityLog,
  UpdateStatusRequest, UpdateRolesRequest
} from '../models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/admin`;

  getDashboardStats(): Observable<DashboardStats> {
    return this.http
      .get<ApiResponse<DashboardStats>>(`${this.base}/dashboard/stats`)
      .pipe(map((r) => r.data!));
  }

  getActivityLogs(page = 0, size = 20): Observable<PageResponse<ActivityLog>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<ApiResponse<PageResponse<ActivityLog>>>(`${this.base}/dashboard/activity`, { params })
      .pipe(map((r) => r.data!));
  }

  getUsers(page = 0, size = 20, search?: string, status?: string): Observable<PageResponse<AdminUser>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) params = params.set('search', search);
    if (status) params = params.set('status', status);
    return this.http
      .get<ApiResponse<PageResponse<AdminUser>>>(`${this.base}/users`, { params })
      .pipe(map((r) => r.data!));
  }

  updateUserStatus(publicId: string, req: UpdateStatusRequest): Observable<AdminUser> {
    return this.http
      .put<ApiResponse<AdminUser>>(`${this.base}/users/${publicId}/status`, req)
      .pipe(map((r) => r.data!));
  }

  updateUserRoles(publicId: string, req: UpdateRolesRequest): Observable<AdminUser> {
    return this.http
      .put<ApiResponse<AdminUser>>(`${this.base}/users/${publicId}/roles`, req)
      .pipe(map((r) => r.data!));
  }

  deleteUser(publicId: string): Observable<void> {
    return this.http
      .delete<void>(`${this.base}/users/${publicId}`);
  }

  getReports(page = 0, size = 20, status?: string): Observable<PageResponse<AdminReport>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http
      .get<ApiResponse<PageResponse<AdminReport>>>(`${this.base}/reports`, { params })
      .pipe(map((r) => r.data!));
  }

  resolveReport(id: number): Observable<AdminReport> {
    return this.http
      .put<ApiResponse<AdminReport>>(`${this.base}/reports/${id}/resolve`, {})
      .pipe(map((r) => r.data!));
  }
}
