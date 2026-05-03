package tn.enicarthage.qartnet.dto.response;

import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
        UUID publicId,
        String username,
        String email,
        String firstName,
        String lastName,
        String bio,
        String profilePictureUrl,
        List<String> skills,
        Map<String, String> socialLinks,
        Set<Role> roles,
        AccountStatus accountStatus,
        boolean emailVerified,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {}
