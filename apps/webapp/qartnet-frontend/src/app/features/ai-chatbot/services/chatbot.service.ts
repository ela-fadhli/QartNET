import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';
import {
  AskChatbotRequest,
  AskChatbotResponse,
  ChatMessageResponse,
} from '../models/chatbot.models';

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/api/ai/chat`;

  ask(message: string): Observable<AskChatbotResponse> {
    const req: AskChatbotRequest = { message };
    return this.http
      .post<ApiResponse<AskChatbotResponse>>(this.api, req)
      .pipe(map((res) => res.data!));
  }

  getHistory(): Observable<ChatMessageResponse[]> {
    return this.http
      .get<ApiResponse<ChatMessageResponse[]>>(`${this.api}/history`)
      .pipe(map((res) => res.data!));
  }

  reset(): Observable<void> {
    return this.http.post<void>(`${this.api}/reset`, {});
  }
}
