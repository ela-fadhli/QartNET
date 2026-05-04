import { ChangeDetectorRef, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { Skeleton } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';
import { InputTextModule } from 'primeng/inputtext';
import { ForumService } from '../../services/forum.service';
import { ForumDetailResponse, Page, ThreadSummaryResponse } from '../../models/forum.models';

@Component({
  selector: 'app-thread-list',
  standalone: true,
  imports: [RouterLink, FormsModule, ButtonModule, Skeleton, MessageModule, InputTextModule],
  templateUrl: './thread-list.html',
})
export class ForumDetailComponent implements OnInit {
  private forumService = inject(ForumService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private destroyRef = inject(DestroyRef);

  forum = signal<ForumDetailResponse | null>(null);
  threadsPage = signal<Page<ThreadSummaryResponse> | null>(null);
  loadingForum = signal(true);
  loadingThreads = signal(true);
  error = signal<string | null>(null);
  activeCategoryId = signal<string | null>(null);
  currentPage = signal(0);
  readonly pageSize = 10;

  // Management panel
  showManage = signal(false);
  newCategoryName = signal('');
  editingForum = signal(false);
  editName = signal('');
  editDescription = signal('');
  deletingForum = signal(false);
  manageSaving = signal(false);
  manageError = signal<string | null>(null);

  private slug = '';

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        this.slug = params.get('slug')!;
        this.loadForum();
      });

    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        this.activeCategoryId.set(params.get('category'));
        this.currentPage.set(0);
        this.loadThreads();
      });
  }

  loadForum(): void {
    this.loadingForum.set(true);
    this.forumService.getForum(this.slug).subscribe({
      next: (forum) => {
        this.forum.set(forum);
        this.loadingForum.set(false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.error.set('Forum not found.');
        this.loadingForum.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  loadThreads(): void {
    if (!this.slug) return;
    this.loadingThreads.set(true);
    this.forumService
      .getThreads(this.slug, this.activeCategoryId() ?? undefined, this.currentPage(), this.pageSize)
      .subscribe({
        next: (page) => {
          this.threadsPage.set(page);
          this.loadingThreads.set(false);
          this.cdr.markForCheck();
        },
        error: () => {
          this.loadingThreads.set(false);
          this.cdr.markForCheck();
        },
      });
  }

  filterByCategory(id: string | null): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { category: id ?? undefined },
      queryParamsHandling: 'merge',
    });
  }

  activeCategoryName(): string | null {
    const id = this.activeCategoryId();
    return id ? (this.forum()?.categories.find((c) => c.publicId === id)?.name ?? null) : null;
  }

  prevPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update((p) => p - 1);
      this.loadThreads();
    }
  }

  nextPage(): void {
    if (!this.threadsPage()?.last) {
      this.currentPage.update((p) => p + 1);
      this.loadThreads();
    }
  }

  toggleManage(): void {
    const f = this.forum();
    if (f && !this.showManage()) {
      this.editName.set(f.name);
      this.editDescription.set(f.description ?? '');
    }
    this.showManage.update((v) => !v);
    this.manageError.set(null);
    this.editingForum.set(false);
    this.deletingForum.set(false);
  }

  addCategory(): void {
    const name = this.newCategoryName().trim();
    if (!name) return;
    this.manageSaving.set(true);
    this.manageError.set(null);
    this.forumService.createCategory(this.slug, name).subscribe({
      next: () => {
        this.newCategoryName.set('');
        this.manageSaving.set(false);
        this.loadForum();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.manageError.set(err.userMessage ?? 'Failed to add category.');
        this.manageSaving.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  removeCategory(catPublicId: string): void {
    this.manageSaving.set(true);
    this.manageError.set(null);
    this.forumService.deleteCategory(this.slug, catPublicId).subscribe({
      next: () => {
        this.manageSaving.set(false);
        this.loadForum();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.manageError.set(err.userMessage ?? 'Failed to delete category.');
        this.manageSaving.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  saveForumSettings(): void {
    this.manageSaving.set(true);
    this.manageError.set(null);
    this.forumService.updateForum(this.slug, {
      name: this.editName().trim() || null,
      description: this.editDescription().trim() || null,
    }).subscribe({
      next: () => {
        this.manageSaving.set(false);
        this.editingForum.set(false);
        this.loadForum();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.manageError.set(err.userMessage ?? 'Failed to update forum.');
        this.manageSaving.set(false);
        this.cdr.markForCheck();
      },
    });
  }

  confirmDeleteForum(): void {
    this.manageSaving.set(true);
    this.forumService.deleteForum(this.slug).subscribe({
      next: () => this.router.navigate(['/forum']),
      error: (err) => {
        this.manageError.set(err.userMessage ?? 'Failed to delete forum.');
        this.manageSaving.set(false);
        this.deletingForum.set(false);
        this.cdr.markForCheck();
      },
    });
  }
}
