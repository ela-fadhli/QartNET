import { ChangeDetectorRef, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { Skeleton } from 'primeng/skeleton';
import { ForumService } from '../../services/forum.service';
import { CategoryResponse, Page, TagResponse, ThreadSummaryResponse } from '../../models/forum.models';

@Component({
  selector: 'app-thread-list',
  standalone: true,
  imports: [RouterLink, ButtonModule, Skeleton],
  templateUrl: './thread-list.html',
})
export class ThreadListComponent implements OnInit {
  private forumService = inject(ForumService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private destroyRef = inject(DestroyRef);

  categories = signal<CategoryResponse[]>([]);
  tags = signal<TagResponse[]>([]);
  threadsPage = signal<Page<ThreadSummaryResponse> | null>(null);
  loading = signal(true);
  activeCategoryId = signal<string | null>(null);
  activeTagId = signal<string | null>(null);
  currentPage = signal(0);
  readonly pageSize = 10;

  ngOnInit(): void {
    this.forumService.getCategories().subscribe((cats) => {
      this.categories.set(cats);
      this.cdr.markForCheck();
    });

    this.forumService.getTags().subscribe((tags) => {
      this.tags.set(tags);
      this.cdr.markForCheck();
    });

    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        this.activeCategoryId.set(params.get('category'));
        this.activeTagId.set(params.get('tag'));
        this.currentPage.set(0);
        this.loadThreads();
      });
  }

  loadThreads(): void {
    this.loading.set(true);
    this.forumService
      .getThreads({
        category: this.activeCategoryId() ?? undefined,
        tag: this.activeTagId() ?? undefined,
        page: this.currentPage(),
        size: this.pageSize,
      })
      .subscribe({
        next: (page) => {
          this.threadsPage.set(page);
          this.loading.set(false);
          this.cdr.markForCheck();
        },
      });
  }

  filterByCategory(id: string | null): void {
    this.router.navigate([], {
      queryParams: { category: id ?? undefined, tag: this.activeTagId() ?? undefined },
      queryParamsHandling: 'merge',
    });
  }

  filterByTag(id: string | null): void {
    this.router.navigate([], {
      queryParams: { tag: id ?? undefined, category: this.activeCategoryId() ?? undefined },
      queryParamsHandling: 'merge',
    });
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

  activeCategoryName(): string | null {
    const id = this.activeCategoryId();
    return id ? (this.categories().find((c) => c.publicId === id)?.name ?? null) : null;
  }
}
