import { ChangeDetectorRef, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { Skeleton } from 'primeng/skeleton';
import { ForumService } from '../../services/forum.service';
import { ForumSummaryResponse, Page } from '../../models/forum.models';

@Component({
  selector: 'app-forum-home',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, ButtonModule, Skeleton],
  templateUrl: './forum-home.html',
})
export class ForumListComponent implements OnInit {
  private forumService = inject(ForumService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private destroyRef = inject(DestroyRef);

  forumsPage = signal<Page<ForumSummaryResponse> | null>(null);
  loading = signal(true);
  currentPage = signal(0);
  readonly pageSize = 12;

  searchControl = new FormControl('');

  ngOnInit(): void {
    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.currentPage.set(0);
        this.loadForums();
      });

    this.loadForums();
  }

  loadForums(): void {
    this.loading.set(true);
    this.forumService
      .getForums(this.searchControl.value ?? '', this.currentPage(), this.pageSize)
      .subscribe({
        next: (page) => {
          this.forumsPage.set(page);
          this.loading.set(false);
          this.cdr.markForCheck();
        },
        error: () => {
          this.loading.set(false);
          this.cdr.markForCheck();
        },
      });
  }

  prevPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update((p) => p - 1);
      this.loadForums();
    }
  }

  nextPage(): void {
    if (!this.forumsPage()?.last) {
      this.currentPage.update((p) => p + 1);
      this.loadForums();
    }
  }
}
