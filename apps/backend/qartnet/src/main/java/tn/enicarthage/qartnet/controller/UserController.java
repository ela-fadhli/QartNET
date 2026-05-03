package tn.enicarthage.qartnet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.request.UpdateProfileRequest;
import tn.enicarthage.qartnet.dto.response.UserProfileResponse;
import tn.enicarthage.qartnet.dto.response.UserPublicProfileResponse;
import tn.enicarthage.qartnet.service.IUserService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final IUserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(principal.getUsername())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateMyProfile(principal.getUsername(), request)));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get a user's public profile")
    public ResponseEntity<ApiResponse<UserPublicProfileResponse>> getPublicProfile(
            @PathVariable UUID publicId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getPublicProfile(publicId)));
    }
}
