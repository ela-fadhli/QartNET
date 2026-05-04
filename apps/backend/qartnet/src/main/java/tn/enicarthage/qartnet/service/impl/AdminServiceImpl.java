package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.dto.request.AdminUpdateUserRolesRequest;
import tn.enicarthage.qartnet.dto.request.AdminUpdateUserStatusRequest;
import tn.enicarthage.qartnet.dto.request.ResolveReportRequest;
import tn.enicarthage.qartnet.dto.response.*;
import tn.enicarthage.qartnet.model.ActivityLog;
import tn.enicarthage.qartnet.model.ContentReport;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.*;
import tn.enicarthage.qartnet.service.IAdminService;
import tn.enicarthage.qartnet.shared.enums.*;
import tn.enicarthage.qartnet.shared.exception.BadRequestException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final UserRepository userRepository;
    private final ContentReportRepository reportRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ForumThreadRepository threadRepository;
    private final ReplyRepository replyRepository;

    @Override
    public Page<UserAdminResponse> listUsers(String search, AccountStatus status, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasStatus = status != null;

        Page<User> page;
        if (!hasSearch && !hasStatus) {
            page = userRepository.findAllActive(pageable);
        } else if (hasSearch && !hasStatus) {
            page = userRepository.findAllActiveBySearch(search, pageable);
        } else if (!hasSearch) {
            page = userRepository.findAllActiveByStatus(status, pageable);
        } else {
            page = userRepository.findAllActiveWithFilters(search, status, pageable);
        }
        return page.map(this::toAdminResponse);
    }

    @Override
    public UserAdminResponse getUserDetail(UUID publicId) {
        User user = findUser(publicId);
        return toAdminResponse(user);
    }

    @Override
    @Transactional
    public UserAdminResponse updateUserStatus(UUID publicId, AdminUpdateUserStatusRequest request, String adminPublicId) {
        User user = findUser(publicId);
        AccountStatus old = user.getAccountStatus();
        user.setAccountStatus(request.status());
        userRepository.save(user);

        logAdminAction(adminPublicId, ActivityType.ADMIN_USER_STATUS_CHANGE,
                "User " + user.getUsername() + " status changed: " + old + " -> " + request.status());

        return toAdminResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID publicId, String adminPublicId) {
        User user = findUser(publicId);
        user.setDeletedAt(LocalDateTime.now());
        user.setAccountStatus(AccountStatus.DISABLED);
        userRepository.save(user);

        logAdminAction(adminPublicId, ActivityType.ADMIN_USER_STATUS_CHANGE,
                "User " + user.getUsername() + " soft-deleted");
    }

    @Override
    @Transactional
    public UserAdminResponse updateUserRoles(UUID publicId, AdminUpdateUserRolesRequest request, String adminPublicId) {
        User user = findUser(publicId);
        user.getRoles().clear();
        user.getRoles().addAll(request.roles());
        userRepository.save(user);

        logAdminAction(adminPublicId, ActivityType.ADMIN_USER_ROLE_CHANGE,
                "User " + user.getUsername() + " roles updated to: " + request.roles());

        return toAdminResponse(user);
    }

    @Override
    public Page<ContentReportResponse> listReports(ReportStatus status, Pageable pageable) {
        if (status != null) {
            return reportRepository.findByStatus(status, pageable).map(this::toReportResponse);
        }
        return reportRepository.findAll(pageable).map(this::toReportResponse);
    }

    @Override
    @Transactional
    public ContentReportResponse resolveReport(Long reportId, ResolveReportRequest request, String adminPublicId) {
        ContentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> ResourceNotFoundException.of("Report", reportId));

        User admin = userRepository.findByPublicId(UUID.fromString(adminPublicId))
                .orElseThrow(() -> ResourceNotFoundException.of("Admin", adminPublicId));

        report.setStatus(request.status());
        report.setResolvedBy(admin);
        report.setResolvedAt(LocalDateTime.now());
        reportRepository.save(report);

        logAdminAction(adminPublicId, ActivityType.ADMIN_REPORT_RESOLVED,
                "Report #" + reportId + " resolved as: " + request.status());

        return toReportResponse(report);
    }

    @Override
    @Transactional
    public void deleteContent(String contentType, Long contentId, String adminPublicId) {
        switch (contentType.toLowerCase()) {
            case "thread" -> {
                if (!threadRepository.existsById(contentId))
                    throw ResourceNotFoundException.of("Thread", contentId);
                threadRepository.deleteById(contentId);
            }
            case "reply" -> {
                if (!replyRepository.existsById(contentId))
                    throw ResourceNotFoundException.of("Reply", contentId);
                replyRepository.deleteById(contentId);
            }
            default -> throw new BadRequestException("Unknown content type: " + contentType);
        }
        logAdminAction(adminPublicId, ActivityType.ADMIN_CONTENT_DELETED,
                "Deleted " + contentType + " #" + contentId);
    }

    @Override
    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();

        long total = userRepository.count();
        long active = userRepository.countByAccountStatus(AccountStatus.ACTIVE);
        long pending = userRepository.countByAccountStatus(AccountStatus.PENDING);
        long suspended = userRepository.countByAccountStatus(AccountStatus.SUSPENDED);
        long disabled = userRepository.countByAccountStatus(AccountStatus.DISABLED);

        long reg7 = userRepository.countByCreatedAtBetween(now.minusDays(7), now);
        long reg30 = userRepository.countByCreatedAtBetween(now.minusDays(30), now);

        Map<String, Long> byRole = new HashMap<>();
        for (Role role : Role.values()) {
            byRole.put(role.name(), userRepository.countByRole(role));
        }

        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        long totalReports = reportRepository.count();

        return new DashboardStatsResponse(
                total, active, pending, suspended, disabled,
                reg7, reg30, byRole, pendingReports, totalReports
        );
    }

    @Override
    public Page<ActivityLogResponse> getActivityLog(Pageable pageable) {
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toActivityResponse);
    }

    private User findUser(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", publicId));
    }

    private void logAdminAction(String adminPublicId, ActivityType type, String details) {
        userRepository.findByPublicId(UUID.fromString(adminPublicId)).ifPresent(admin -> {
            activityLogRepository.save(ActivityLog.builder()
                    .user(admin)
                    .action(type)
                    .details(details)
                    .build());
        });
    }

    private UserAdminResponse toAdminResponse(User u) {
        return new UserAdminResponse(
                u.getId(), u.getPublicId(), u.getUsername(), u.getEmail(),
                u.getFirstName(), u.getLastName(), u.getRoles(),
                u.getAccountStatus(), u.isEmailVerified(),
                u.getCreatedAt(), u.getUpdatedAt(), u.getLastLoginAt(), u.getDeletedAt()
        );
    }

    private ContentReportResponse toReportResponse(ContentReport r) {
        return new ContentReportResponse(
                r.getId(),
                r.getReporter() != null ? r.getReporter().getPublicId() : null,
                r.getReporter() != null ? r.getReporter().getUsername() : null,
                r.getContentType(), r.getContentId(), r.getReason(), r.getStatus(),
                r.getResolvedBy() != null ? r.getResolvedBy().getPublicId() : null,
                r.getResolvedBy() != null ? r.getResolvedBy().getUsername() : null,
                r.getResolvedAt(), r.getCreatedAt()
        );
    }

    private ActivityLogResponse toActivityResponse(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(), log.getAction(),
                log.getUser() != null ? log.getUser().getPublicId() : null,
                log.getUser() != null ? log.getUser().getUsername() : null,
                log.getDetails(), log.getIpAddress(), log.getCreatedAt()
        );
    }
}
