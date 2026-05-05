import {
  AfterViewChecked,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  OnInit,
  computed,
  effect,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { Skeleton } from 'primeng/skeleton';
import { AvatarModule } from 'primeng/avatar';
import { Subscription } from 'rxjs';
import { MessagingService } from '../../services/messaging.service';
import {
  ConversationSummaryResponse,
  MessageResponse,
} from '../../models/messaging.models';

@Component({
  selector: 'app-messaging-home',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    Skeleton,
    AvatarModule,
  ],
  templateUrl: './messaging-home.html',
})
export class MessagingHomeComponent implements OnInit, AfterViewChecked {
  private messagingService = inject(MessagingService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private destroyRef = inject(DestroyRef);

  conversations = signal<ConversationSummaryResponse[]>([]);
  loadingConversations = signal(true);

  activeConversationId = signal<string | null>(null);
  activeConversation = computed(() =>
    this.conversations().find((c) => c.publicId === this.activeConversationId()) ?? null,
  );

  messages = signal<MessageResponse[]>([]);
  loadingMessages = signal(false);

  composer = new FormControl('', { nonNullable: true });
  sending = signal(false);

  myPublicId = signal<string | null>(null);

  private messagesEnd = viewChild<ElementRef<HTMLDivElement>>('messagesEnd');
  private wsSub: Subscription | null = null;
  private pendingScroll = false;

  constructor() {
    effect(() => {
      const id = this.activeConversationId();
      if (id) this.loadConversationContext(id);
      else {
        this.messages.set([]);
        this.unsubscribeFromConversation();
      }
    });
  }

  ngOnInit(): void {
    this.myPublicId.set(this.deriveMyPublicId());

    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        const id = params.get('conversationPublicId');
        this.activeConversationId.set(id);
      });

    this.loadConversations();

    // Cross-conversation badge updates: bump unread or refresh list when a
    // message arrives for a conversation that isn't currently open.
    this.messagingService
      .onUserMessages()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((msg) => this.handleIncomingForBadges(msg));
  }

  ngAfterViewChecked(): void {
    if (this.pendingScroll) {
      this.scrollToBottom();
      this.pendingScroll = false;
    }
  }

  loadConversations(): void {
    this.loadingConversations.set(true);
    this.messagingService.listConversations().subscribe({
      next: (list) => {
        this.conversations.set(list);
        this.loadingConversations.set(false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadingConversations.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  selectConversation(id: string): void {
    this.router.navigate(['/messaging', id]);
  }

  send(): void {
    const text = this.composer.value.trim();
    const conv = this.activeConversation();
    if (!text || !conv || this.sending()) return;
    this.sending.set(true);
    this.composer.setValue('');

    // Use HTTP so we get a deterministic response and a clean error path.
    // The backend also broadcasts via STOMP, so other tabs/users get it live.
    this.messagingService.sendMessageHttp(conv.publicId, text).subscribe({
      next: () => {
        this.sending.set(false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.sending.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  onComposerKey(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  otherParticipantName(conv: ConversationSummaryResponse): string {
    const me = this.myPublicId();
    const other = conv.participants.find((p) => p.userPublicId !== me) ?? conv.participants[0];
    return other?.username ?? 'Conversation';
  }

  isMine(msg: MessageResponse): boolean {
    return msg.senderPublicId === this.myPublicId();
  }

  // ── Internal helpers ──────────────────────────────────────────

  private loadConversationContext(conversationPublicId: string): void {
    this.loadingMessages.set(true);
    this.messages.set([]);

    this.messagingService.listMessages(conversationPublicId).subscribe({
      next: (msgs) => {
        this.messages.set(msgs);
        this.loadingMessages.set(false);
        this.pendingScroll = true;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadingMessages.set(false);
        this.cdr.markForCheck();
      },
    });

    this.messagingService.markRead(conversationPublicId).subscribe({
      next: () => this.clearUnread(conversationPublicId),
    });

    this.unsubscribeFromConversation();
    this.wsSub = this.messagingService
      .onConversationMessages(conversationPublicId)
      .subscribe((msg) => this.appendMessage(msg));
  }

  private unsubscribeFromConversation(): void {
    this.wsSub?.unsubscribe();
    this.wsSub = null;
  }

  private appendMessage(msg: MessageResponse): void {
    if (this.messages().some((m) => m.publicId === msg.publicId)) return;
    this.messages.update((list) => [...list, msg]);
    this.pendingScroll = true;

    // If the message belongs to the open conversation and isn't ours, mark read.
    if (!this.isMine(msg)) {
      this.messagingService.markRead(msg.conversationPublicId).subscribe({
        next: () => this.clearUnread(msg.conversationPublicId),
      });
    }
    this.cdr.markForCheck();
  }

  private handleIncomingForBadges(msg: MessageResponse): void {
    const isActive = msg.conversationPublicId === this.activeConversationId();
    if (isActive) return; // already handled by the topic stream
    if (msg.senderPublicId === this.myPublicId()) return;

    this.conversations.update((list) =>
      list.map((c) =>
        c.publicId === msg.conversationPublicId
          ? { ...c, unreadCount: c.unreadCount + 1, lastMessage: msg }
          : c,
      ),
    );
    this.cdr.markForCheck();
  }

  private clearUnread(conversationPublicId: string): void {
    this.conversations.update((list) =>
      list.map((c) =>
        c.publicId === conversationPublicId ? { ...c, unreadCount: 0 } : c,
      ),
    );
    this.cdr.markForCheck();
  }

  private scrollToBottom(): void {
    const el = this.messagesEnd()?.nativeElement;
    el?.scrollIntoView({ behavior: 'auto', block: 'end' });
  }

  private deriveMyPublicId(): string | null {
    const token = localStorage.getItem('qartnet_token');
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.publicId ?? payload.sub ?? null;
    } catch {
      return null;
    }
  }
}
