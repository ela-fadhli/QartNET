import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { WebSocketService } from '../../../core/services/websocket.service';
import {
  ConversationSummaryResponse,
  MessageResponse,
  SendMessageRequest,
} from '../models/messaging.models';

@Injectable({ providedIn: 'root' })
export class MessagingService {
  private http = inject(HttpClient);
  private ws = inject(WebSocketService);
  private api = `${environment.apiUrl}/api/messaging`;

  listConversations(): Observable<ConversationSummaryResponse[]> {
    return this.http
      .get<ApiResponse<ConversationSummaryResponse[]>>(`${this.api}/conversations`)
      .pipe(map((res) => res.data!));
  }

  openDirectConversation(userPublicId: string): Observable<ConversationSummaryResponse> {
    return this.http
      .post<ApiResponse<ConversationSummaryResponse>>(
        `${this.api}/conversations/direct/${userPublicId}`,
        {},
      )
      .pipe(map((res) => res.data!));
  }

  listMessages(conversationPublicId: string, before?: string, limit = 50): Observable<MessageResponse[]> {
    let params = new HttpParams().set('limit', limit);
    if (before) params = params.set('before', before);
    return this.http
      .get<ApiResponse<MessageResponse[]>>(
        `${this.api}/conversations/${conversationPublicId}/messages`,
        { params },
      )
      .pipe(map((res) => res.data!));
  }

  sendMessageHttp(conversationPublicId: string, body: string): Observable<MessageResponse> {
    const req: SendMessageRequest = { body };
    return this.http
      .post<ApiResponse<MessageResponse>>(
        `${this.api}/conversations/${conversationPublicId}/messages`,
        req,
      )
      .pipe(map((res) => res.data!));
  }

  sendMessageWs(conversationPublicId: string, body: string): void {
    this.ws.publish(`/app/conversation.${conversationPublicId}.send`, { body });
  }

  markRead(conversationPublicId: string): Observable<void> {
    return this.http.post<void>(
      `${this.api}/conversations/${conversationPublicId}/read`,
      {},
    );
  }

  /** Stream of new messages within a single conversation. */
  onConversationMessages(conversationPublicId: string): Observable<MessageResponse> {
    return this.ws
      .subscribe(`/topic/conversation.${conversationPublicId}`)
      .pipe(map((frame) => JSON.parse(frame.body) as MessageResponse));
  }

  /** Stream of new messages across all conversations the user participates in. */
  onUserMessages(): Observable<MessageResponse> {
    return this.ws
      .subscribe('/user/queue/messages')
      .pipe(map((frame) => JSON.parse(frame.body) as MessageResponse));
  }
}
