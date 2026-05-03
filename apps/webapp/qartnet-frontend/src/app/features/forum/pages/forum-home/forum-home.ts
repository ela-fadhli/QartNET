import { ChangeDetectorRef, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { Skeleton } from 'primeng/skeleton';
import { ForumService } from '../../services/forum.service';
import { CategoryResponse, ThreadSummaryResponse } from '../../models/forum.models';

@Component({
  selector: 'app-forum-home',
  standalone: true,
  imports: [RouterLink, ButtonModule, Skeleton],
  templateUrl: './forum-home.html',
})
export class ForumHomeComponent implements OnInit {
  private forumService = inject(ForumService);
  private cdr = inject(ChangeDetectorRef);

  categories = signal<CategoryResponse[]>([]);
  recentThreads = signal<ThreadSummaryResponse[]>([]);
  loadingCategories = signal(true);
  loadingThreads = signal(true);

  ngOnInit(): void {
    this.forumService.getCategories().subscribe({
      next: (cats) => {
        this.categories.set(cats);
        this.loadingCategories.set(false);
        this.cdr.markForCheck();
      },
    });

    this.forumService.getThreads({ page: 0, size: 5 }).subscribe({
      next: (page) => {
        this.recentThreads.set(page.content);
        this.loadingThreads.set(false);
        this.cdr.markForCheck();
      },
    });
  }
}
