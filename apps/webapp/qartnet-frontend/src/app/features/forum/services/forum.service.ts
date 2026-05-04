import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { environment } from '../../../../environments/environment';
import {
  CategoryResponse,
  TagResponse,
  ThreadSummaryResponse,
  ThreadDetailResponse,
  ReplyResponse,
  CreateThreadRequest,
  CreateReplyRequest,
  Page,
} from '../models/forum.models';

@Injectable({ providedIn: 'root' })
export class ForumService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/forum`;

  getCategories(): Observable<CategoryResponse[]> {
    return this.http
      .get<ApiResponse<CategoryResponse[]>>(`${this.baseUrl}/categories`)
      .pipe(map((res) => res.data!));
  }

  getTags(): Observable<TagResponse[]> {
    return this.http
      .get<ApiResponse<TagResponse[]>>(`${this.baseUrl}/tags`)
      .pipe(map((res) => res.data!));
  }

  getThreads(params: {
    category?: string;
    tag?: string;
    page?: number;
    size?: number;
  }): Observable<Page<ThreadSummaryResponse>> {
    let httpParams = new HttpParams();
    if (params.category) httpParams = httpParams.set('category', params.category);
    if (params.tag) httpParams = httpParams.set('tag', params.tag);
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page);
    if (params.size !== undefined) httpParams = httpParams.set('size', params.size);

    return this.http
      .get<ApiResponse<Page<ThreadSummaryResponse>>>(`${this.baseUrl}/threads`, { params: httpParams })
      .pipe(map((res) => res.data!));
  }

  getThread(publicId: string): Observable<ThreadDetailResponse> {
    return this.http
      .get<ApiResponse<ThreadDetailResponse>>(`${this.baseUrl}/threads/${publicId}`)
      .pipe(map((res) => res.data!));
  }

  createThread(request: CreateThreadRequest): Observable<ThreadSummaryResponse> {
    return this.http
      .post<ApiResponse<ThreadSummaryResponse>>(`${this.baseUrl}/threads`, request)
      .pipe(map((res) => res.data!));
  }

  createReply(threadPublicId: string, request: CreateReplyRequest): Observable<ReplyResponse> {
    return this.http
      .post<ApiResponse<ReplyResponse>>(`${this.baseUrl}/threads/${threadPublicId}/replies`, request)
      .pipe(map((res) => res.data!));
  }

  deleteReply(publicId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/replies/${publicId}`)
      .pipe(map(() => void 0));
  }
}
