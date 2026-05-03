package tn.enicarthage.qartnet.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ThreadSummaryResponse(
        UUID publicId,
        String title,
        String authorUsername,
        CategoryResponse category,
        List<TagResponse> tags,
        long viewCount,
        int replyCount,
        LocalDateTime createdAt
) {}