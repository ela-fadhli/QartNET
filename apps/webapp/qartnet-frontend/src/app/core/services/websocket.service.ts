import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client | null = null;

  connect(token: string): void {
    if (this.client?.active) return;

    this.client = new Client({
      brokerURL: environment.wsUrl,
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

  /**
   * Subscribes to a STOMP destination. If the client is still connecting,
   * the subscription is registered through the client's onConnect hook so
   * callers don't need to manually wait for activation.
   */
  subscribe(destination: string): Observable<IMessage> {
    return new Observable(observer => {
      let sub: StompSubscription | null = null;
      let cancelled = false;

      const doSubscribe = () => {
        if (cancelled || !this.client) return;
        sub = this.client.subscribe(destination, msg => observer.next(msg));
      };

      if (this.client?.connected) {
        doSubscribe();
      } else if (this.client) {
        const previous = this.client.onConnect;
        this.client.onConnect = (frame) => {
          previous?.(frame);
          doSubscribe();
        };
      } else {
        observer.error(new Error('WebSocket client not initialized; call connect() first'));
        return;
      }

      return () => {
        cancelled = true;
        sub?.unsubscribe();
      };
    });
  }

  publish(destination: string, body: object): void {
    this.client?.publish({
      destination,
      body: JSON.stringify(body),
    });
  }
}
