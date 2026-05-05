package tn.enicarthage.qartnet.dto.response;

import tn.enicarthage.qartnet.shared.enums.ChatRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageResponse(
        UUID publicId,
        ChatRole role,
        String content,
        LocalDateTime createdAt
) {}
