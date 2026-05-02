import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client | null = null;

  connect(token: string): void {
    if (this.client?.active) return;

    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl) as WebSocket,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
    });

    this.client.activate();
  }

  disconnect(): void {
    this.client?.deactivate();
    this.client = null;
  }

  get connected(): boolean {
    return this.client?.connected ?? false;
  }

  subscribe(destination: string): Observable<IMessage> {
    return new Observable(observer => {
      if (!this.client?.connected) {
        observer.error(new Error('WebSocket not connected'));
        return;
      }
      const sub: StompSubscription = this.client.subscribe(destination, msg => observer.next(msg));
      return () => sub.unsubscribe();
    });
  }

  publish(destination: string, body: object): void {
    this.client?.publish({
      destination,
      body: JSON.stringify(body),
    });
  }
}
