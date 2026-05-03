import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { environment } from '../../../../environments/environment';
import {
  ForumSummaryResponse,
  ForumDetailResponse,
  ForumCategoryResponse,
  ThreadSummaryResponse,
  ThreadDetailResponse,
  ReplyResponse,
  CreateForumRequest,
  CreateThreadRequest,
  CreateReplyRequest,
  Page,
} from '../models/forum.models';

@Injectable({ providedIn: 'root' })
export class ForumService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/api`;

  // ── Forums ────────────────────────────────────────────────────

  getForums(query = '', page = 0, size = 12): Observable<Page<ForumSummaryResponse>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page)
      .set('size', size);
    return this.http
      .get<ApiResponse<Page<ForumSummaryResponse>>>(`${this.api}/forums`, { params })
      .pipe(map((res) => res.data!));
  }

  getForum(slug: string): Observable<ForumDetailResponse> {
    return this.http
      .get<ApiResponse<ForumDetailResponse>>(`${this.api}/forums/${slug}`)
      .pipe(map((res) => res.data!));
  }

  createForum(req: CreateForumRequest): Observable<ForumSummaryResponse> {
    return this.http
      .post<ApiResponse<ForumSummaryResponse>>(`${this.api}/forums`, req)
      .pipe(map((res) => res.data!));
  }

  // ── Categories ────────────────────────────────────────────────

  getCategories(slug: string): Observable<ForumCategoryResponse[]> {
    return this.http
      .get<ApiResponse<ForumCategoryResponse[]>>(`${this.api}/forums/${slug}/categories`)
      .pipe(map((res) => res.data!));
  }

  // ── Threads ───────────────────────────────────────────────────

  getThreads(
    slug: string,
    categoryPublicId?: string,
    page = 0,
    size = 10,
  ): Observable<Page<ThreadSummaryResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (categoryPublicId) params = params.set('categoryPublicId', categoryPublicId);
    return this.http
      .get<ApiResponse<Page<ThreadSummaryResponse>>>(`${this.api}/forums/${slug}/threads`, { params })
      .pipe(map((res) => res.data!));
  }

  getThread(publicId: string): Observable<ThreadDetailResponse> {
    return this.http
      .get<ApiResponse<ThreadDetailResponse>>(`${this.api}/threads/${publicId}`)
      .pipe(map((res) => res.data!));
  }

  createThread(slug: string, req: CreateThreadRequest): Observable<ThreadDetailResponse> {
    return this.http
      .post<ApiResponse<ThreadDetailResponse>>(`${this.api}/forums/${slug}/threads`, req)
      .pipe(map((res) => res.data!));
  }

  deleteThread(publicId: string): Observable<void> {
    return this.http
      .delete<void>(`${this.api}/threads/${publicId}`);
  }

  // ── Replies ───────────────────────────────────────────────────

  createReply(threadPublicId: string, req: CreateReplyRequest): Observable<ReplyResponse> {
    return this.http
      .post<ApiResponse<ReplyResponse>>(`${this.api}/threads/${threadPublicId}/replies`, req)
      .pipe(map((res) => res.data!));
  }

  deleteReply(publicId: string): Observable<void> {
    return this.http
      .delete<void>(`${this.api}/replies/${publicId}`);
  }
}
