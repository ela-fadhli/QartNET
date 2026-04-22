import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';

export interface AdminMetrics {
  totalUsers: number;
  totalUsersChange: string;
  activeToday: number;
  activeTodayChange: string;
  repositories: number;
  repositoriesChange: string;
  forumPosts: number;
  forumPostsChange: string;
  groups: number;
  groupsChange: string;
  storageUsedValue: number;
  storageUsedTotal: number;
}

export interface UserRow {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  department: string;
  joined: string;
  contributions: number;
  initials: string;
}

export interface ActivityLogEvent {
  id: string;
  icon: string;
  iconBg: string;
  title: string;
  description: string;
  time: string;
}

export interface ContentReport {
  id: string;
  type: string;
  status: 'Pending' | 'Reviewing' | 'Resolved';
  title: string;
  reportedBy: string;
  time: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminMockService {

  getMetrics(): Observable<AdminMetrics> {
    const data: AdminMetrics = {
      totalUsers: 847,
      totalUsersChange: '+23 this week',
      activeToday: 312,
      activeTodayChange: '37% of users',
      repositories: 1243,
      repositoriesChange: '+12 this week',
      forumPosts: 5678,
      forumPostsChange: '+89 this week',
      groups: 89,
      groupsChange: '+3 this week',
      storageUsedValue: 45.2,
      storageUsedTotal: 100
    };
    return of(data).pipe(delay(300));
  }

  getUsers(): Observable<UserRow[]> {
    const users: UserRow[] = [
      { id: '1', firstName: 'Ela', lastName: 'Fadhli', email: 'ela.fadhli@enicar.ucar.tn', role: 'Student', department: 'Computer Science', joined: '2024-09-01', contributions: 347, initials: 'EF' },
      { id: '2', firstName: 'Azer', lastName: 'Kouka', email: 'azer.kouka@enicar.ucar.tn', role: 'Student', department: 'Software Engineering', joined: '2024-09-15', contributions: 215, initials: 'AK' },
      { id: '3', firstName: 'Prof.', lastName: 'Faouzi Jaidi', email: 'faouzi.jaidi@enicar.ucar.tn', role: 'Teacher', department: 'Computer Science', joined: '2023-01-10', contributions: 89, initials: 'PFJ' },
      { id: '4', firstName: 'Lina', lastName: 'Zerhouni', email: 'l.zerhouni@univ.edu', role: 'Student', department: 'Data Science', joined: '2024-10-01', contributions: 178, initials: 'LZ' },
      { id: '5', firstName: 'Idriss', lastName: 'Hadad', email: 'idriss.hadad@enicar.ucar.tn', role: 'Student', department: 'Cybersecurity', joined: '2024-09-20', contributions: 95, initials: 'IH' },
      { id: '6', firstName: 'Prof.', lastName: 'Imen Kammoun', email: 'imen.kammoun@enicar.ucar.tn', role: 'Teacher', department: 'Software Engineering', joined: '2022-09-01', contributions: 42, initials: 'PIK' }
    ];
    return of(users).pipe(delay(500));
  }

  getActivityLog(): Observable<ActivityLogEvent[]> {
    const events: ActivityLogEvent[] = [
      {
        id: '1',
        icon: 'pi pi-user-plus',
        iconBg: 'bg-primary-50 text-primary-600',
        title: 'New user registered',
        description: 'lina.zerhouni@univ.edu',
        time: '10 minutes ago'
      },
      {
        id: '2',
        icon: 'pi pi-folder',
        iconBg: 'bg-indigo-50 text-indigo-600',
        title: 'Repository created',
        description: 'secure-auth-module by Yassine Khelifi',
        time: '1 hour ago'
      },
      {
        id: '3',
        icon: 'pi pi-exclamation-triangle',
        iconBg: 'bg-orange-50 text-orange-600',
        title: 'Content reported',
        description: 'Forum post in Backend category',
        time: '2 hours ago'
      },
      {
        id: '4',
        icon: 'pi pi-ban',
        iconBg: 'bg-red-50 text-red-600',
        title: 'User suspended',
        description: 'spam-account@univ.edu',
        time: '5 hours ago'
      },
      {
        id: '5',
        icon: 'pi pi-users',
        iconBg: 'bg-purple-50 text-purple-600',
        title: 'New group created',
        description: 'AI Research Lab by Lina Zerhouni',
        time: '1 day ago'
      },
      {
        id: '6',
        icon: 'pi pi-database',
        iconBg: 'bg-cyan-50 text-cyan-600',
        title: 'System backup completed',
        description: 'Full database backup (4.2 GB)',
        time: '1 day ago'
      }
    ];
    return of(events).pipe(delay(200));
  }

  getContentReports(): Observable<ContentReport[]> {
    const reports: ContentReport[] = [
      {
        id: '1',
        type: 'Post',
        status: 'Pending',
        title: 'Inappropriate language in forum discussion',
        reportedBy: 'Reported by Sara Moursd · 2 hours ago',
        time: '2 hours ago'
      },
      {
        id: '2',
        type: 'Comment',
        status: 'Pending',
        title: 'Spam content promoting external services',
        reportedBy: 'Reported by Yassine Khelifi · 5 hours ago',
        time: '5 hours ago'
      },
      {
        id: '3',
        type: 'Repository',
        status: 'Reviewing',
        title: 'Copied project without attribution',
        reportedBy: 'Reported by Prof. Karim Hadj · 1 day ago',
        time: '1 day ago'
      },
      {
        id: '4',
        type: 'Post',
        status: 'Pending',
        title: 'Off-topic content in academic channel',
        reportedBy: 'Reported by Ela Fadhli · 2 days ago',
        time: '2 days ago'
      },
      {
        id: '5',
        type: 'Comment',
        status: 'Reviewing',
        title: 'Harassment in project discussion thread',
        reportedBy: 'Reported by Azer Kouka · 3 days ago',
        time: '3 days ago'
      }
    ];
    return of(reports).pipe(delay(200));
  }
}
