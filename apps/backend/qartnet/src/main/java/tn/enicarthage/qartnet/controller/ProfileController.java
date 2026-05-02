package tn.enicarthage.qartnet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.request.UpdateProfileRequest;
import tn.enicarthage.qartnet.dto.response.ProfileResponse;
import tn.enicarthage.qartnet.service.IProfileService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(Authentication auth) {
        UUID publicId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(profileService.getMyProfile(publicId)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMyProfile(
            Authentication auth,
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID publicId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(profileService.updateMyProfile(publicId, request)));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getPublicProfile(@PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getPublicProfile(username)));
    }
}
