package tn.enicarthage.qartnet.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ForumSummaryResponse(
        UUID publicId, String name, String slug, String description, String banner,
        String ownerUsername, long threadCount, LocalDateTime createdAt
) {}
