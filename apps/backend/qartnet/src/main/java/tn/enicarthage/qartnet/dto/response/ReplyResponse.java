package tn.enicarthage.qartnet.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReplyResponse(
        UUID publicId,
        String body,
        String authorUsername,
        UUID parentReplyPublicId,
        LocalDateTime createdAt
) {}