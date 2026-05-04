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
  UpdateForumRequest,
  CreateThreadRequest,
  UpdateThreadRequest,
  CreateReplyRequest,
  UpdateReplyRequest,
  Page,
} from '../models/forum.models';

@Injectable({ providedIn: 'root' })
export class ForumService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/api`;

  // ── Forums ────────────────────────────────────────────────────

  getForums(query = '', page = 0, size = 12): Observable<Page<ForumSummaryResponse>> {
    const params = new HttpParams()
      .set('q', query)
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

  updateForum(slug: string, req: UpdateForumRequest): Observable<ForumSummaryResponse> {
    return this.http
      .patch<ApiResponse<ForumSummaryResponse>>(`${this.api}/forums/${slug}`, req)
      .pipe(map((res) => res.data!));
  }

  deleteForum(slug: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/forums/${slug}`);
  }

  // ── Categories ────────────────────────────────────────────────

  getCategories(slug: string): Observable<ForumCategoryResponse[]> {
    return this.http
      .get<ApiResponse<ForumCategoryResponse[]>>(`${this.api}/forums/${slug}/categories`)
      .pipe(map((res) => res.data!));
  }

  createCategory(slug: string, name: string): Observable<ForumCategoryResponse> {
    return this.http
      .post<ApiResponse<ForumCategoryResponse>>(`${this.api}/forums/${slug}/categories`, { name })
      .pipe(map((res) => res.data!));
  }

  deleteCategory(slug: string, catPublicId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/forums/${slug}/categories/${catPublicId}`);
  }

  // ── Threads ───────────────────────────────────────────────────

  getThreads(
    slug: string,
    categoryPublicId?: string,
    page = 0,
    size = 10,
  ): Observable<Page<ThreadSummaryResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (categoryPublicId) params = params.set('category', categoryPublicId);
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

  updateThread(publicId: string, req: UpdateThreadRequest): Observable<ThreadDetailResponse> {
    return this.http
      .patch<ApiResponse<ThreadDetailResponse>>(`${this.api}/threads/${publicId}`, req)
      .pipe(map((res) => res.data!));
  }

  deleteThread(publicId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/threads/${publicId}`);
  }

  // ── Replies ───────────────────────────────────────────────────

  createReply(threadPublicId: string, req: CreateReplyRequest): Observable<ReplyResponse> {
    return this.http
      .post<ApiResponse<ReplyResponse>>(`${this.api}/threads/${threadPublicId}/replies`, req)
      .pipe(map((res) => res.data!));
  }

  updateReply(publicId: string, req: UpdateReplyRequest): Observable<ReplyResponse> {
    return this.http
      .patch<ApiResponse<ReplyResponse>>(`${this.api}/replies/${publicId}`, req)
      .pipe(map((res) => res.data!));
  }

  deleteReply(publicId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/replies/${publicId}`);
  }
}
