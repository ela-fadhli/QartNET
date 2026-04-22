import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { TagModule } from 'primeng/tag';
import { MenuItem } from 'primeng/api';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

import {
  AdminMockService,
  AdminMetrics,
  UserRow,
  ActivityLogEvent,
  ContentReport
} from '../../../core/services/admin.mock.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    TableModule,
    InputTextModule,
    ButtonModule,
    AvatarModule,
    MenuModule,
    TagModule,
    IconFieldModule,
    InputIconModule
  ],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  private adminService = inject(AdminMockService);

  metrics: AdminMetrics | null = null;
  users: UserRow[] = [];
  filteredUsers: UserRow[] = [];
  searchQuery: string = '';

  activityLog: ActivityLogEvent[] = [];
  contentReports: ContentReport[] = [];

  activeTab: string = 'users';

  actionItems: MenuItem[] = [
    { label: 'Edit details', icon: 'pi pi-pencil' },
    { label: 'Reset password', icon: 'pi pi-refresh' },
    { separator: true },
    { label: 'Suspend user', icon: 'pi pi-ban', styleClass: 'text-red-500' }
  ];

  selectedUser: UserRow | null = null;

  ngOnInit() {
    this.adminService.getMetrics().subscribe(data => this.metrics = data);
    this.adminService.getUsers().subscribe(data => {
      this.users = data;
      this.filteredUsers = data;
    });
    this.adminService.getActivityLog().subscribe(data => this.activityLog = data);
    this.adminService.getContentReports().subscribe(data => this.contentReports = data);
  }

  filterUsers() {
    if (!this.searchQuery) {
      this.filteredUsers = this.users;
      return;
    }
    const query = this.searchQuery.toLowerCase();
    this.filteredUsers = this.users.filter(u =>
      u.firstName.toLowerCase().includes(query) ||
      u.lastName.toLowerCase().includes(query) ||
      u.email.toLowerCase().includes(query)
    );
  }

  onActionClick(user: UserRow) {
    this.selectedUser = user;
  }

  dismissReport(report: ContentReport) {
    this.contentReports = this.contentReports.filter(r => r.id !== report.id);
  }

  takeAction(report: ContentReport) {
    report.status = 'Reviewing';
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Pending':   return 'bg-red-100 text-red-600';
      case 'Reviewing': return 'bg-amber-100 text-amber-600';
      case 'Resolved':  return 'bg-green-100 text-green-600';
      default:          return 'bg-surface-100 text-surface-600';
    }
  }

  get pendingReportsCount(): number {
    return this.contentReports.filter(r => r.status === 'Pending').length;
  }
}
