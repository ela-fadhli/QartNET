package tn.enicarthage.qartnet.dto.response;

import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserAdminResponse(
        Long id,
        UUID publicId,
        String username,
        String email,
        String firstName,
        String lastName,
        Set<Role> roles,
        AccountStatus accountStatus,
        boolean emailVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt,
        LocalDateTime deletedAt
) {}
