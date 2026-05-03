import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmationService, MessageService } from 'primeng/api';
import { AdminService } from '../../services/admin.service';
import { AdminUser } from '../../models/admin.models';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [FormsModule, DatePipe, ButtonModule, InputTextModule, SelectModule, TagModule, TooltipModule],
  templateUrl: './users-list.html',
})
export class AdminUsersComponent implements OnInit {
  private adminService = inject(AdminService);
  private messageService = inject(MessageService);
  private confirmService = inject(ConfirmationService);

  users = signal<AdminUser[]>([]);
  loading = signal(true);
  totalElements = signal(0);
  page = signal(0);
  pageSize = 20;

  searchQuery = '';
  selectedStatus = '';

  statusOptions = [
    { label: 'All statuses', value: '' },
    { label: 'Active', value: 'ACTIVE' },
    { label: 'Pending', value: 'PENDING' },
    { label: 'Suspended', value: 'SUSPENDED' },
    { label: 'Disabled', value: 'DISABLED' },
  ];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.adminService.getUsers(this.page(), this.pageSize, this.searchQuery || undefined, this.selectedStatus || undefined)
      .subscribe({
        next: (p) => {
          this.users.set(p.content);
          this.totalElements.set(p.totalElements);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.messageService.add({
            severity: 'error',
            summary: 'Failed to load users',
            detail: err?.error?.message ?? `HTTP ${err?.status}`,
            life: 5000,
          });
        },
      });
  }

  search(): void {
    this.page.set(0);
    this.load();
  }

  nextPage(): void {
    this.page.update((p) => p + 1);
    this.load();
  }

  prevPage(): void {
    this.page.update((p) => Math.max(0, p - 1));
    this.load();
  }

  get totalPages(): number {
    return Math.ceil(this.totalElements() / this.pageSize);
  }

  toggleStatus(user: AdminUser): void {
    const next = user.accountStatus === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    this.confirmService.confirm({
      message: `Set ${user.username} to ${next}?`,
      header: 'Confirm',
      accept: () => {
        this.adminService.updateUserStatus(user.publicId, { status: next }).subscribe({
          next: (updated) => {
            this.users.update((list) => list.map((u) => u.publicId === updated.publicId ? updated : u));
            this.messageService.add({ severity: 'success', summary: 'Status updated', life: 3000 });
          },
          error: () => this.messageService.add({ severity: 'error', summary: 'Update failed', life: 3000 }),
        });
      },
    });
  }

  deleteUser(user: AdminUser): void {
    this.confirmService.confirm({
      message: `Permanently delete ${user.username}?`,
      header: 'Delete user',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.adminService.deleteUser(user.publicId).subscribe({
          next: () => {
            this.users.update((list) => list.filter((u) => u.publicId !== user.publicId));
            this.messageService.add({ severity: 'success', summary: 'User deleted', life: 3000 });
          },
          error: () => this.messageService.add({ severity: 'error', summary: 'Delete failed', life: 3000 }),
        });
      },
    });
  }

  getStatusSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    const map: Record<string, 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast'> = {
      ACTIVE: 'success', PENDING: 'warn', SUSPENDED: 'danger', DISABLED: 'secondary'
    };
    return map[status] ?? 'secondary';
  }

  getRoleSeverity(role: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    const map: Record<string, 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast'> = {
      ADMIN: 'danger', TEACHER: 'info', STUDENT: 'secondary'
    };
    return map[role] ?? 'secondary';
  }
}
