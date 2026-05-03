package tn.enicarthage.qartnet.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.response.ActivityLogResponse;
import tn.enicarthage.qartnet.dto.response.DashboardStatsResponse;
import tn.enicarthage.qartnet.service.IAdminService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Dashboard", description = "Statistics and activity log")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final IAdminService adminService;

    @GetMapping("/stats")
    @Operation(summary = "Get platform statistics for the dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    @GetMapping("/activity")
    @Operation(summary = "Get recent activity log")
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> getActivity(
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getActivityLog(pageable)));
    }
}
