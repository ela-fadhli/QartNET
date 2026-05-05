import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { MessageService } from 'primeng/api';
import { AdminService } from '../../services/admin.service';
import { AdminReport } from '../../models/admin.models';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [DatePipe, ButtonModule, TagModule],
  templateUrl: './reports.html',
})
export class AdminReportsComponent implements OnInit {
  private adminService = inject(AdminService);
  private messageService = inject(MessageService);

  reports = signal<AdminReport[]>([]);
  loading = signal(true);
  totalElements = signal(0);
  page = signal(0);
  pageSize = 20;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.adminService.getReports(this.page(), this.pageSize).subscribe({
      next: (p) => {
        this.reports.set(p.content);
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

  resolve(report: AdminReport): void {
    this.updateReportStatus(report, 'RESOLVED', 'Report resolved', 'Failed to resolve report');
  }

  reject(report: AdminReport): void {
    this.updateReportStatus(report, 'DISMISSED', 'Report rejected', 'Failed to reject report');
  }

  cancelResolution(report: AdminReport): void {
    this.updateReportStatus(report, 'PENDING', 'Resolution canceled', 'Failed to cancel resolution');
  }

  private updateReportStatus(
    report: AdminReport,
    status: 'PENDING' | 'RESOLVED' | 'DISMISSED',
    successSummary: string,
    errorSummary: string,
  ): void {
    this.adminService.resolveReport(report.id, status).subscribe({
      next: (updated) => {
        this.reports.update((list) => list.map((r) => r.id === updated.id ? updated : r));
        this.messageService.add({ severity: 'success', summary: successSummary, life: 3000 });
      },
      error: (err) => this.messageService.add({
        severity: 'error',
        summary: errorSummary,
        detail: err?.error?.message ?? `HTTP ${err?.status}`,
        life: 4000,
      }),
    });
  }

  getStatusSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    const map: Record<string, 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast'> = {
      PENDING: 'warn', RESOLVED: 'success', DISMISSED: 'secondary'
    };
    return map[status] ?? 'secondary';
  }
}
