import {
  AfterViewChecked,
  ChangeDetectorRef,
  Component,
  ElementRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ChatbotService } from '../../services/chatbot.service';
import { ChatMessageResponse } from '../../models/chatbot.models';

@Component({
  selector: 'app-ai-chatbot',
  standalone: true,
  imports: [ReactiveFormsModule, ButtonModule, InputTextModule],
  templateUrl: './ai-chatbot.html',
})
export class AiChatbotComponent implements AfterViewChecked {
  private chatbotService = inject(ChatbotService);
  private cdr = inject(ChangeDetectorRef);

  open = signal(false);
  loadingHistory = signal(false);
  sending = signal(false);
  messages = signal<ChatMessageResponse[]>([]);
  errorMessage = signal<string | null>(null);

  composer = new FormControl('', { nonNullable: true });

  private messagesEnd = viewChild<ElementRef<HTMLDivElement>>('messagesEnd');
  private pendingScroll = false;

  ngAfterViewChecked(): void {
    if (this.pendingScroll) {
      this.scrollToBottom();
      this.pendingScroll = false;
    }
  }

  toggle(): void {
    const next = !this.open();
    this.open.set(next);
    if (next && this.messages().length === 0) this.loadHistory();
  }

  send(): void {
    const text = this.composer.value.trim();
    if (!text || this.sending()) return;

    this.errorMessage.set(null);
    this.sending.set(true);
    this.composer.setValue('');

    // Optimistically render the user message; the server response will overwrite it
    // with the persisted one and append the assistant reply.
    const optimisticId = `tmp-${Date.now()}`;
    this.messages.update((list) => [
      ...list,
      {
        publicId: optimisticId,
        role: 'USER',
        content: text,
        createdAt: new Date().toISOString(),
      },
    ]);
    this.pendingScroll = true;

    this.chatbotService.ask(text).subscribe({
      next: (res) => {
        this.messages.update((list) => [
          ...list.filter((m) => m.publicId !== optimisticId),
          res.userMessage,
          res.assistantMessage,
        ]);
        this.sending.set(false);
        this.pendingScroll = true;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.messages.update((list) => list.filter((m) => m.publicId !== optimisticId));
        const msg = err?.error?.message ?? 'AI assistant is unavailable. Try again.';
        this.errorMessage.set(msg);
        this.sending.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  reset(): void {
    this.chatbotService.reset().subscribe({
      next: () => {
        this.messages.set([]);
        this.errorMessage.set(null);
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

  isUser(msg: ChatMessageResponse): boolean {
    return msg.role === 'USER';
  }

  private loadHistory(): void {
    this.loadingHistory.set(true);
    this.chatbotService.getHistory().subscribe({
      next: (msgs) => {
        this.messages.set(msgs);
        this.loadingHistory.set(false);
        this.pendingScroll = true;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadingHistory.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  private scrollToBottom(): void {
    this.messagesEnd()?.nativeElement.scrollIntoView({ behavior: 'auto', block: 'end' });
  }
}
