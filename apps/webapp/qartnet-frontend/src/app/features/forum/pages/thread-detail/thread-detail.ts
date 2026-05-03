import { ChangeDetectorRef, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import { Skeleton } from 'primeng/skeleton';
import { ForumService } from '../../services/forum.service';
import { ReplyResponse, ThreadDetailResponse } from '../../models/forum.models';

@Component({
  selector: 'app-thread-detail',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, ButtonModule, TextareaModule, MessageModule, Skeleton],
  templateUrl: './thread-detail.html',
})
export class ThreadDetailComponent implements OnInit {
  private forumService = inject(ForumService);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  thread = signal<ThreadDetailResponse | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);
  submitting = signal(false);
  replyError = signal<string | null>(null);
  replyingTo = signal<ReplyResponse | null>(null);

  replyForm = this.fb.group({
    body: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(2000)]],
  });

  get body() { return this.replyForm.get('body')!; }

  topLevelReplies(replies: ReplyResponse[]): ReplyResponse[] {
    return replies.filter((r) => !r.parentReplyPublicId);
  }

  childrenOf(publicId: string, replies: ReplyResponse[]): ReplyResponse[] {
    return replies.filter((r) => r.parentReplyPublicId === publicId);
  }

  ngOnInit(): void {
    const publicId = this.route.snapshot.paramMap.get('publicId')!;
    this.forumService.getThread(publicId).subscribe({
      next: (thread) => {
        this.thread.set(thread);
        this.loading.set(false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.error.set('Thread not found.');
        this.loading.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  setReplyingTo(reply: ReplyResponse | null): void {
    this.replyingTo.set(reply);
    this.replyError.set(null);
    this.replyForm.reset();
  }

  submitReply(): void {
    if (this.replyForm.invalid) {
      this.replyForm.markAllAsTouched();
      return;
    }
    const thread = this.thread();
    if (!thread) return;

    this.submitting.set(true);
    this.replyError.set(null);

    this.forumService
      .createReply(thread.publicId, {
        body: this.body.value!,
        parentReplyPublicId: this.replyingTo()?.publicId ?? null,
      })
      .subscribe({
        next: (reply) => {
          this.thread.set({ ...thread, replies: [...thread.replies, reply] });
          this.replyForm.reset();
          this.replyingTo.set(null);
          this.submitting.set(false);
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.replyError.set(err.userMessage ?? 'Failed to post reply.');
          this.submitting.set(false);
          this.cdr.markForCheck();
        },
      });
  }
}
