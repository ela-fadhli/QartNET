package tn.enicarthage.qartnet.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ThreadSummaryResponse(
        UUID publicId, String title, String authorUsername,
        String forumSlug, String forumName,
        ForumCategoryResponse category, List<TagResponse> tags,
        long viewCount, long replyCount, LocalDateTime createdAt
) {}