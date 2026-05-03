package tn.enicarthage.qartnet.dto.response;

import java.util.Map;

public record DashboardStatsResponse(
        long totalUsers,
        long activeUsers,
        long pendingUsers,
        long suspendedUsers,
        long disabledUsers,
        long registrationsLast7Days,
        long registrationsLast30Days,
        Map<String, Long> usersByRole,
        long pendingReports,
        long totalReports
) {}
