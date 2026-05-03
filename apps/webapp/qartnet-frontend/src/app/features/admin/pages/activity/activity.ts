import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { AdminService } from '../../services/admin.service';
import { ActivityLog } from '../../models/admin.models';

@Component({
  selector: 'app-admin-activity',
  standalone: true,
  imports: [DatePipe, ButtonModule],
  templateUrl: './activity.html',
})
export class AdminActivityComponent implements OnInit {
  private adminService = inject(AdminService);

  logs = signal<ActivityLog[]>([]);
  loading = signal(true);
  totalElements = signal(0);
  page = signal(0);
  pageSize = 30;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.adminService.getActivityLogs(this.page(), this.pageSize).subscribe({
      next: (p) => {
        this.logs.set(p.content);
        this.totalElements.set(p.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  get totalPages(): number {
    return Math.ceil(this.totalElements() / this.pageSize);
  }

  nextPage(): void { this.page.update((p) => p + 1); this.load(); }
  prevPage(): void { this.page.update((p) => Math.max(0, p - 1)); this.load(); }

  getActionColor(action: string): string {
    if (action.includes('LOGIN')) return '#4ade80';
    if (action.includes('REGISTER')) return '#C9A84C';
    if (action.includes('SUSPEND') || action.includes('DELETE')) return '#f87171';
    if (action.includes('RESET') || action.includes('PASSWORD')) return '#facc15';
    return 'rgba(232, 213, 163, 0.5)';
  }
}
