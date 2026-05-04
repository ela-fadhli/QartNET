import { ChangeDetectorRef, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { Skeleton } from 'primeng/skeleton';
import { ForumService } from '../../services/forum.service';
import { ProfileService } from '../../../profile/services/profile.service';
import { ReplyResponse, ThreadDetailResponse } from '../../models/forum.models';

@Component({
  selector: 'app-thread-detail',
  standalone: true,
  imports: [RouterLink, FormsModule, ReactiveFormsModule, ButtonModule, TextareaModule, InputTextModule, MessageModule, Skeleton],
  templateUrl: './thread-detail.html',
})
export class ThreadDetailComponent implements OnInit {
  private forumService = inject(ForumService);
  private profileService = inject(ProfileService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  thread = signal<ThreadDetailResponse | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);
  submitting = signal(false);
  replyError = signal<string | null>(null);
  replyingTo = signal<ReplyResponse | null>(null);

  currentUsername = signal<string | null>(null);

  // Thread edit/delete
  editingThread = signal(false);
  editTitle = signal('');
  editBody = signal('');
  savingThread = signal(false);
  deletingThread = signal(false);
  threadActionError = signal<string | null>(null);

  // Reply edit/delete
  editingReplyId = signal<string | null>(null);
  editReplyBody = signal('');
  savingReply = signal(false);

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
    this.profileService.getMyProfile().subscribe({
      next: (p) => {
        this.currentUsername.set(p.username);
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

  // ── Thread edit/delete ──────────────────────────────────────

  startEditThread(): void {
    const t = this.thread();
    if (!t) return;
    this.editTitle.set(t.title);
    this.editBody.set(t.body);
    this.editingThread.set(true);
    this.threadActionError.set(null);
  }

  cancelEditThread(): void {
    this.editingThread.set(false);
    this.threadActionError.set(null);
  }

  saveThread(): void {
    const t = this.thread();
    if (!t) return;
    this.savingThread.set(true);
    this.threadActionError.set(null);
    this.forumService.updateThread(t.publicId, {
      title: this.editTitle().trim() || null,
      body: this.editBody().trim() || null,
    }).subscribe({
      next: (updated) => {
        this.thread.set(updated);
        this.editingThread.set(false);
        this.savingThread.set(false);
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.threadActionError.set(err.userMessage ?? 'Failed to save changes.');
        this.savingThread.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  confirmDeleteThread(): void {
    const t = this.thread();
    if (!t) return;
    this.forumService.deleteThread(t.publicId).subscribe({
      next: () => this.router.navigate(['/forum', t.forumSlug]),
      error: (err) => {
        this.threadActionError.set(err.userMessage ?? 'Failed to delete thread.');
        this.deletingThread.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  // ── Reply edit/delete ───────────────────────────────────────

  startEditReply(reply: ReplyResponse): void {
    this.editingReplyId.set(reply.publicId);
    this.editReplyBody.set(reply.body);
  }

  cancelEditReply(): void {
    this.editingReplyId.set(null);
    this.editReplyBody.set('');
  }

  saveReply(replyPublicId: string): void {
    const body = this.editReplyBody().trim();
    if (!body) return;
    this.savingReply.set(true);
    this.forumService.updateReply(replyPublicId, { body }).subscribe({
      next: (updated) => {
        const t = this.thread();
        if (t) {
          this.thread.set({
            ...t,
            replies: t.replies.map((r) => r.publicId === replyPublicId ? updated : r),
          });
        }
        this.editingReplyId.set(null);
        this.savingReply.set(false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.savingReply.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  deleteReply(replyPublicId: string): void {
    this.forumService.deleteReply(replyPublicId).subscribe({
      next: () => {
        const t = this.thread();
        if (t) {
          this.thread.set({
            ...t,
            replies: t.replies.filter((r) => r.publicId !== replyPublicId && r.parentReplyPublicId !== replyPublicId),
          });
        }
        this.cdr.markForCheck();
      },
    });
  }
}
