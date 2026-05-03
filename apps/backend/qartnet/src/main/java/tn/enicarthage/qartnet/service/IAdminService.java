package tn.enicarthage.qartnet.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.enicarthage.qartnet.dto.request.AdminUpdateUserRolesRequest;
import tn.enicarthage.qartnet.dto.request.AdminUpdateUserStatusRequest;
import tn.enicarthage.qartnet.dto.request.ResolveReportRequest;
import tn.enicarthage.qartnet.dto.response.*;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.ReportStatus;

import java.util.UUID;

public interface IAdminService {

    Page<UserAdminResponse> listUsers(String search, AccountStatus status, Pageable pageable);

    UserAdminResponse getUserDetail(UUID publicId);

    UserAdminResponse updateUserStatus(UUID publicId, AdminUpdateUserStatusRequest request, String adminPublicId);

    void deleteUser(UUID publicId, String adminPublicId);

    UserAdminResponse updateUserRoles(UUID publicId, AdminUpdateUserRolesRequest request, String adminPublicId);

    Page<ContentReportResponse> listReports(ReportStatus status, Pageable pageable);

    ContentReportResponse resolveReport(Long reportId, ResolveReportRequest request, String adminPublicId);

    void deleteContent(String contentType, Long contentId, String adminPublicId);

    DashboardStatsResponse getDashboardStats();

    Page<ActivityLogResponse> getActivityLog(Pageable pageable);
}
