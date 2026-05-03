package tn.enicarthage.qartnet.dto.response;

import tn.enicarthage.qartnet.shared.enums.ActivityType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityLogResponse(
        Long id,
        ActivityType action,
        UUID userPublicId,
        String username,
        String details,
        String ipAddress,
        LocalDateTime createdAt
) {}
