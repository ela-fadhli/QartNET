package tn.enicarthage.qartnet.dto.response;

import tn.enicarthage.qartnet.shared.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID publicId,
        NotificationType type,
        String title,
        String body,
        String link,
        boolean read,
        LocalDateTime createdAt
) {}
