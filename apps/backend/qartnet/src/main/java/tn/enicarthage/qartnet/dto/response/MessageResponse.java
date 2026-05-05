package tn.enicarthage.qartnet.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID publicId,
        UUID conversationPublicId,
        UUID senderPublicId,
        String senderUsername,
        String body,
        LocalDateTime sentAt,
        LocalDateTime editedAt
) {}
