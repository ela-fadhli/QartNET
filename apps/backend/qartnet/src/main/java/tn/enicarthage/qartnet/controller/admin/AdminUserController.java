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
import tn.enicarthage.qartnet.dto.request.AdminUpdateUserRolesRequest;
import tn.enicarthage.qartnet.dto.request.AdminUpdateUserStatusRequest;
import tn.enicarthage.qartnet.dto.response.UserAdminResponse;
import tn.enicarthage.qartnet.service.IAdminService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Users", description = "Backoffice user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final IAdminService adminService;

    @GetMapping
    @Operation(summary = "List all users with optional search/status filter")
    public ResponseEntity<ApiResponse<Page<UserAdminResponse>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AccountStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listUsers(search, status, pageable)));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get full details of a user")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUserDetail(@PathVariable UUID publicId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserDetail(publicId)));
    }

    @PutMapping("/{publicId}/status")
    @Operation(summary = "Change a user's account status")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateStatus(
            @PathVariable UUID publicId,
            @Valid @RequestBody AdminUpdateUserStatusRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.updateUserStatus(publicId, request, principal.getUsername())));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Soft-delete a user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal UserDetails principal) {
        adminService.deleteUser(publicId, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PutMapping("/{publicId}/roles")
    @Operation(summary = "Update a user's roles")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateRoles(
            @PathVariable UUID publicId,
            @Valid @RequestBody AdminUpdateUserRolesRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.updateUserRoles(publicId, request, principal.getUsername())));
    }
}
