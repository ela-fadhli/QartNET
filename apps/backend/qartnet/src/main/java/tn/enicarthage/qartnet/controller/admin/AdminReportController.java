package tn.enicarthage.qartnet.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.request.ResolveReportRequest;
import tn.enicarthage.qartnet.dto.response.ContentReportResponse;
import tn.enicarthage.qartnet.service.IAdminService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;
import tn.enicarthage.qartnet.shared.enums.ReportStatus;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Moderation", description = "Content reports and moderation")
@SecurityRequirement(name = "bearerAuth")
public class AdminReportController {

    private final IAdminService adminService;

    @GetMapping("/reports")
    @Operation(summary = "List all content reports, optionally filtered by status")
    public ResponseEntity<ApiResponse<Page<ContentReportResponse>>> listReports(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listReports(status, pageable)));
    }

    @PutMapping("/reports/{id}/resolve")
    @Operation(summary = "Resolve a content report")
    public ResponseEntity<ApiResponse<ContentReportResponse>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ResolveReportRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.resolveReport(id, request, principal.getUsername())));
    }

    @DeleteMapping("/content/{contentType}/{contentId}")
    @Operation(summary = "Delete or hide a piece of content")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @PathVariable String contentType,
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserDetails principal) {
        adminService.deleteContent(contentType, contentId, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Content removed successfully", null));
    }
}
