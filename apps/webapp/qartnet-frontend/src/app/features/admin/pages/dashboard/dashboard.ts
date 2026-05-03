import { Component, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe, KeyValuePipe } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { DashboardStats } from '../../models/admin.models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [DecimalPipe, KeyValuePipe],
  templateUrl: './dashboard.html',
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminService);

  stats = signal<DashboardStats | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    this.adminService.getDashboardStats().subscribe({
      next: (s) => { this.stats.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
