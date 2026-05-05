export interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  pendingUsers: number;
  suspendedUsers: number;
  disabledUsers: number;
  registrationsLast7Days: number;
  registrationsLast30Days: number;
  usersByRole: Record<string, number>;
  pendingReports: number;
  totalReports: number;
}

export interface AdminUser {
  publicId: string;
  username: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  roles: string[];
  accountStatus: string;
  emailVerified: boolean;
  createdAt: string;
  lastLoginAt: string | null;
}

export interface AdminReport {
  id: number;
  reporterUsername: string;
  contentType: string;
  contentId: number;
  reason: string;
  status: string;
  createdAt: string;
  resolvedByUsername: string | null;
  resolvedAt: string | null;
}

export interface ActivityLog {
  id: number;
  action: string;
  username: string | null;
  details: string | null;
  ipAddress: string | null;
  createdAt: string;
}

export interface UpdateStatusRequest {
  status: string;
}

export interface UpdateRolesRequest {
  roles: string[];
}
